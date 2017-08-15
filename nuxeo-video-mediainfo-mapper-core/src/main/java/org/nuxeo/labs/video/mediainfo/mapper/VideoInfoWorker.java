package org.nuxeo.labs.video.mediainfo.mapper;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.IdRef;
import org.nuxeo.ecm.core.api.NuxeoException;
import org.nuxeo.ecm.core.api.model.Property;
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.EventService;
import org.nuxeo.ecm.core.event.impl.DocumentEventContext;
import org.nuxeo.ecm.core.event.impl.EventContextImpl;
import org.nuxeo.ecm.core.work.AbstractWork;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.transaction.TransactionHelper;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import static org.nuxeo.ecm.platform.video.VideoConstants.*;

public class VideoInfoWorker extends AbstractWork {

    private static final long serialVersionUID = 1L;

    private static final Log log = LogFactory.getLog(VideoInfoWorker.class);

    public VideoInfoWorker(String repositoryName, String docId) {
        super(repositoryName + ':' + docId + ":VideoInfo");
        setDocument(repositoryName, docId);
    }

    @Override
    public void work() {

        setProgress(Progress.PROGRESS_INDETERMINATE);
        setStatus("Extracting");

        if (!TransactionHelper.isTransactionActive()) {
            startTransaction();
        }

        openSystemSession();
        if (!session.exists(new IdRef(docId))) {
            setStatus("Nothing to process");
            return;
        }

        DocumentModel doc = session.getDocument(new IdRef(docId));
        Property origVideoProperty = doc.getProperty("file:content");
        Blob video = (Blob) origVideoProperty.getValue();

        if (video == null) {
            setStatus("Nothing to process");
            return;
        }

        updateVideoInfo(doc, video);

        doc.setPropertyValue(TRANSCODED_VIDEOS_PROPERTY, null);
        doc.setPropertyValue(STORYBOARD_PROPERTY, null);

        session.saveDocument(doc);

        EventContextImpl evctx = new DocumentEventContext(session, session.getPrincipal(), doc);
        Event eventToSend = evctx.newEvent(VIDEO_CHANGED_EVENT);
        EventService eventService = Framework.getLocalService(EventService.class);
        eventService.fireEvent(eventToSend);

        setStatus("Done");
    }

    @Override
    public String getTitle() {
        return "VideoInfoWorker-" + docId;
    }

    public void updateVideoInfo(DocumentModel doc, Blob blob) throws NuxeoException{
        Map<String, Map<String, String>> mediainfo = MediaInfoHelper.getProcessedMediaInfo(blob);

        Map<String,Serializable> result = new HashMap<>();
        Map<String,String> generalInfo = mediainfo.get("General");
        Map<String,String> videoInfo = mediainfo.get("Video");

        result.put("duration",Double.parseDouble(generalInfo.get("Duration"))/1000);
        result.put("format",generalInfo.get("Format"));
        result.put("width",Long.parseLong(videoInfo.get("Width")));
        result.put("height",Long.parseLong(videoInfo.get("Height")));
        result.put("frameRate",Double.parseDouble(videoInfo.get("FrameRate")));

        doc.setPropertyValue("vid:info", (Serializable) result);
    }

}