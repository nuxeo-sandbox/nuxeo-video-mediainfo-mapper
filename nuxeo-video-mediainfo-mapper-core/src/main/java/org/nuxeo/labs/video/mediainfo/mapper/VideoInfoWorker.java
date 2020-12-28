/*
 * (C) Copyright 2020 Nuxeo (http://nuxeo.com/) and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 *     Michael Vachette
 */

package org.nuxeo.labs.video.mediainfo.mapper;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.automation.AutomationService;
import org.nuxeo.ecm.automation.OperationChain;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.OperationException;
import org.nuxeo.ecm.core.api.*;
import org.nuxeo.ecm.core.api.model.Property;
import org.nuxeo.ecm.core.work.AbstractWork;
import org.nuxeo.ecm.core.work.api.WorkManager;
import org.nuxeo.ecm.platform.video.service.VideoService;
import org.nuxeo.ecm.platform.video.service.VideoStoryboardWork;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.transaction.TransactionHelper;

import static org.nuxeo.ecm.platform.video.VideoConstants.HAS_STORYBOARD_FACET;
import static org.nuxeo.ecm.platform.video.VideoConstants.HAS_VIDEO_PREVIEW_FACET;

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

        updateVideoInfo(doc);
        session.saveDocument(doc);

        if (doc.hasFacet(HAS_VIDEO_PREVIEW_FACET) && doc.hasFacet(HAS_STORYBOARD_FACET)) {
            // schedule storyboard work
            WorkManager workManager = Framework.getService(WorkManager.class);
            VideoStoryboardWork work = new VideoStoryboardWork(doc.getRepositoryName(), doc.getId());
            log.debug(String.format("Scheduling work: storyboard of Video document %s.", doc));
            workManager.schedule(work, true);
        }

        // schedule conversion work
        VideoService videoService = Framework.getService(VideoService.class);
        log.debug(String.format("Launching automatic conversions of Video document %s.", doc));
        videoService.launchAutomaticConversions(doc);

        setStatus("Done");
    }

    @Override
    public String getTitle() {
        return "VideoInfoWorker-" + docId;
    }

    public void updateVideoInfo(DocumentModel doc) throws NuxeoException{
        CoreSession session = doc.getCoreSession();
        AutomationService as = Framework.getService(AutomationService.class);
        OperationContext octx = new OperationContext();
        octx.setInput(doc);
        octx.setCoreSession(session);
        OperationChain chain = new OperationChain("MediaInfoMappingWorker");
        chain.add("javascript.MediaInfoMapping");
        try {
            doc = (DocumentModel) as.run(octx, chain);
            session.saveDocument(doc);
        } catch (OperationException e) {
            log.error(e);
            throw new NuxeoException(e);
        }
    }

}