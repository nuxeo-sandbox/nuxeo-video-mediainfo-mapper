package org.nuxeo.labs.video.mediainfo.mapper;

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

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.util.Map;

@RunWith(FeaturesRunner.class)
@Features(AutomationFeature.class)
@RepositoryConfig(init = DefaultRepositoryInit.class, cleanup = Granularity.METHOD)
@Deploy({ "nuxeo-video-mediainfo-mapper-core", "org.nuxeo.ecm.platform.video.core"})
public class TestOperation {

    @Inject
    CoreSession session;

    @Test
    public void testOperation() throws IOException, OperationException {

        File file = new File(getClass().getResource("/files/nuxeo.3gp").getPath());
        Blob blob = new FileBlob(file);

        AutomationService as = Framework.getService(AutomationService.class);
        OperationContext ctx = new OperationContext();
        ctx.setInput(blob);
        ctx.setCoreSession(session);
        OperationChain chain = new OperationChain("TestMediaInfoOp");
        chain.add(MediaInfoOp.ID).set("outputVariable", "myVariable");
        blob = (Blob) as.run(ctx, chain);

        Map<String, Map<String, String>> info = (Map<String, Map<String, String>>) ctx.get("myVariable");
        Assert.assertNotNull(info);
        Assert.assertTrue(info.size()>0);
    }
}

