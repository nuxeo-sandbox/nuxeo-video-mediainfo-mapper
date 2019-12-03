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
 */
 package org.nuxeo.labs.video.mediainfo.mapper;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.NuxeoException;

import java.util.Map;

@Operation(id = MediaInfoOp.ID, category = Constants.CAT_BLOB, label = "Extract Metadata using Media Info", description = "Extract the media file metadata using Media Info and store those in a context variable. If outputVariableJsonStr is passed, the JSON string of the result is stored in this variable.")
public class MediaInfoOp {

    public static final String ID = "Blob.ExtractMediaMetadata";

    @Context
    protected OperationContext ctx;

    @Param(name = "outputVariable", required = false)
    protected String outputVariable;

    @Param(name = "outputVariableJsonStr", required = false)
    protected String outputVariableJsonStr;

    @OperationMethod
    public Blob run(Blob blob) {

        if (StringUtils.isBlank(outputVariable) && StringUtils.isBlank(outputVariableJsonStr)) {
            throw new NuxeoException("At least one parameter must be passed");
        }

        Map<String, Map<String, String>> info = MediaInfoHelper.getProcessedMediaInfo(blob);
        if (StringUtils.isNotBlank(outputVariable)) {
            ctx.put(outputVariable, info);
        }

        if (StringUtils.isNotBlank(outputVariableJsonStr)) {
            ctx.put(outputVariableJsonStr, new JSONObject(info).toString());
        }

        return blob;
    }
}