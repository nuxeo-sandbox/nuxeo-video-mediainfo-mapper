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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.automation.OperationException;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.impl.blob.FileBlob;
import org.nuxeo.ecm.core.test.DefaultRepositoryInit;
import org.nuxeo.ecm.core.event.EventService;
import org.nuxeo.ecm.core.test.annotations.Granularity;
import org.nuxeo.ecm.core.test.annotations.RepositoryConfig;
import org.nuxeo.ecm.platform.test.PlatformFeature;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;

@RunWith(FeaturesRunner.class)
@Features(PlatformFeature.class)
@RepositoryConfig(init = DefaultRepositoryInit.class, cleanup = Granularity.METHOD)
@Deploy({ "nuxeo-video-mediainfo-mapper-core", "org.nuxeo.ecm.platform.video.core"})
public class TestVideoChangedListener {

    @Inject
    CoreSession session;

    @Inject
    protected EventService eventService;

    @Test
    public void testListener() throws IOException, OperationException {
        File file = new File(getClass().getResource("/files/nuxeo.3gp").getPath());
        Blob blob = new FileBlob(file);

        DocumentModel doc = session.createDocumentModel("/","testWorker","Video");
        doc.setPropertyValue("file:content", (Serializable) blob);
        doc = session.createDocument(doc);
        session.save();

        eventService.waitForAsyncCompletion();
    }

}
