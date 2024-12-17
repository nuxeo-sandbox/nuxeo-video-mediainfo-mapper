/*
 * (C) Copyright 2018 Nuxeo SA (http://nuxeo.com/) and others.
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
 *     Frédéric Vadon
 *     Thibaud Arguillere
 *     Michael Vachette
 */
package org.nuxeo.labs.video.mediainfo.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.automation.AutomationService;
import org.nuxeo.ecm.automation.OperationChain;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.OperationException;
import org.nuxeo.ecm.automation.test.AutomationFeature;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.impl.blob.FileBlob;
import org.nuxeo.ecm.core.test.DefaultRepositoryInit;
import org.nuxeo.ecm.core.test.annotations.Granularity;
import org.nuxeo.ecm.core.test.annotations.RepositoryConfig;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;

import jakarta.inject.Inject;
import java.io.File;
import java.util.Map;

@RunWith(FeaturesRunner.class)
@Features(AutomationFeature.class)
@RepositoryConfig(init = DefaultRepositoryInit.class, cleanup = Granularity.METHOD)
@Deploy({
        "nuxeo-video-mediainfo-mapper-core",
        "org.nuxeo.ecm.platform.tag",
        "org.nuxeo.ecm.platform.video"
})
public class TestOperation {

    @Inject
    CoreSession session;

    @Test
    public void testOperation() throws OperationException, JsonProcessingException {

        File file = new File(getClass().getResource("/files/nuxeo.3gp").getPath());
        Blob blob = new FileBlob(file);

        AutomationService as = Framework.getService(AutomationService.class);
        OperationContext ctx = new OperationContext();
        ctx.setInput(blob);
        ctx.setCoreSession(session);
        OperationChain chain = new OperationChain("TestMediaInfoOp");
        chain.add(MediaInfoOp.ID).set("outputVariableJsonStr", "myVariableJSONstring");
        as.run(ctx, chain);

        String infoStr = (String) ctx.get("myVariableJSONstring");
        Assert.assertNotNull(infoStr);
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode node = objectMapper.readTree(infoStr);
        Assert.assertTrue(node.size()>0);
    }
}

