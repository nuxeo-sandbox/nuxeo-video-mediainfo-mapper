/*
 * (C) Copyright 2015-2017 Nuxeo (http://nuxeo.com/) and others.
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

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.automation.test.AutomationFeature;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.impl.blob.FileBlob;
import org.nuxeo.ecm.core.test.DefaultRepositoryInit;
import org.nuxeo.ecm.core.test.annotations.Granularity;
import org.nuxeo.ecm.core.test.annotations.RepositoryConfig;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;

import javax.inject.Inject;
import java.io.File;
import java.io.Serializable;
import java.util.Map;

import static org.nuxeo.ecm.platform.video.VideoConstants.INFO_PROPERTY;

@RunWith(FeaturesRunner.class)
@Features(AutomationFeature.class)
@RepositoryConfig(init = DefaultRepositoryInit.class, cleanup = Granularity.METHOD)
@Deploy({
        "nuxeo-video-mediainfo-mapper-core",
        "org.nuxeo.ecm.platform.tag",
        "org.nuxeo.ecm.platform.video.api",
        "org.nuxeo.ecm.platform.video.core"
})
public class TestVideoInfoWorker {

    @Inject
    CoreSession session;

    @Test
    public void testWorker() {
        File file = new File(getClass().getResource("/files/nuxeo.3gp").getPath());
        Blob blob = new FileBlob(file);

        DocumentModel doc = session.createDocumentModel("/","testWorker","Video");
        doc.setPropertyValue("file:content", (Serializable) blob);
        doc = session.createDocument(doc);

        VideoInfoWorker worker = new VideoInfoWorker(session.getRepositoryName(),doc.getId());
        worker.updateVideoInfo(doc);
        Map<String,Serializable> videoInfo = (Map<String, Serializable>) doc.getPropertyValue(INFO_PROPERTY);
        Assert.assertNotNull(videoInfo);
        Assert.assertTrue(VideoInfoTestHelper.isVideoInfoCorrect(videoInfo));
    }

}
