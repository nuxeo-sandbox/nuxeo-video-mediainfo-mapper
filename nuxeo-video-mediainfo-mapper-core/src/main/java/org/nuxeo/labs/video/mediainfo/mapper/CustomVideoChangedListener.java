package org.nuxeo.labs.video.mediainfo.mapper;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.model.Property;
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.EventContext;
import org.nuxeo.ecm.core.event.EventListener;
import org.nuxeo.ecm.core.event.impl.DocumentEventContext;
import org.nuxeo.ecm.core.work.api.WorkManager;
import org.nuxeo.runtime.api.Framework;

import static org.nuxeo.ecm.core.api.event.DocumentEventTypes.BEFORE_DOC_UPDATE;
import static org.nuxeo.ecm.core.api.event.DocumentEventTypes.DOCUMENT_CREATED;
import static org.nuxeo.ecm.platform.picture.api.ImagingDocumentConstants.PICTURE_VIEWS_PROPERTY;
import static org.nuxeo.ecm.platform.video.VideoConstants.*;

public class CustomVideoChangedListener implements EventListener {

    private static final Log log = LogFactory.getLog(CustomVideoChangedListener.class);

    @Override
    public void handleEvent(Event event) {
        EventContext ctx = event.getContext();
        if (!(ctx instanceof DocumentEventContext)) {
            return;
        }
        DocumentEventContext docCtx = (DocumentEventContext) ctx;
        DocumentModel doc = docCtx.getSourceDocument();
        if (doc.hasFacet(HAS_VIDEO_PREVIEW_FACET) && !doc.isProxy() && !doc.isVersion()) {
            boolean forceGeneration = Boolean.TRUE.equals(doc.getContextData(CTX_FORCE_INFORMATIONS_GENERATION));
            Property origVideoProperty = doc.getProperty("file:content");
            if (forceGeneration || DOCUMENT_CREATED.equals(event.getName()) || origVideoProperty.isDirty()) {

                Blob video = (Blob) origVideoProperty.getValue();

                if (BEFORE_DOC_UPDATE.equals(event.getName())) {
                    doc.setPropertyValue(TRANSCODED_VIDEOS_PROPERTY, null);
                    doc.setPropertyValue(STORYBOARD_PROPERTY, null);
                    doc.setPropertyValue(PICTURE_VIEWS_PROPERTY, null);
                }

                // only trigger the event if we really have a video
                if (video != null) {
                    VideoInfoWorker work = new VideoInfoWorker(doc.getRepositoryName(), doc.getId());
                    WorkManager workManager = Framework.getService(WorkManager.class);
                    workManager.schedule(work, WorkManager.Scheduling.IF_NOT_SCHEDULED, true);
                }
            }
        }
    }

}