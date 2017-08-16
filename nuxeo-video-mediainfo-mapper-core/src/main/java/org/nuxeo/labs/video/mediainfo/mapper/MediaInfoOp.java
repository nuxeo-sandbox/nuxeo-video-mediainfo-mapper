package org.nuxeo.labs.video.mediainfo.mapper;

import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.core.api.Blob;

import java.util.Map;

@Operation(id = MediaInfoOp.ID, category = Constants.CAT_BLOB, label = "Extract Metadata using Media Info",
        description = "Extract the media file metadata using Media Info and store those in a context variable.")
public class MediaInfoOp {

    public static final String ID = "Blob.ExtractMediaMetadata";

    @Context
    protected OperationContext ctx;

    @Param(name = "outputVariable", required = true)
    protected String outputVariable;

    @OperationMethod
    public Blob run(Blob blob) {
        Map<String, Map<String, String>> info = MediaInfoHelper.getProcessedMediaInfo(blob);
        ctx.put(outputVariable,info);
        return blob;
    }
}