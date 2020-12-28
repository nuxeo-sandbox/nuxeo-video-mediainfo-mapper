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
 *     Frédéric Vadon
 *     Thibaud Arguillere
 *     Michael Vachette
 */
 package org.nuxeo.labs.video.mediainfo.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.core.api.Blob;

import java.util.Map;

@Operation(
        id = MediaInfoOp.ID,
        category = Constants.CAT_BLOB,
        label = "Extract Metadata using Media Info",
        description = "Extract the media file metadata using Media Info and store those in a context variable. ")
public class MediaInfoOp {

    public static final String ID = "Blob.ExtractMediaMetadata";

    @Context
    protected OperationContext ctx;

    @Param(name = "outputVariable", required = false)
    protected String outputVariable;

    @Param(name = "outputVariableJsonStr", required = false)
    protected String outputVariableJsonStr;

    @OperationMethod
    public Blob run(Blob blob) throws JsonProcessingException {
        JsonNode info = MediaInfoHelper.getProcessedMediaInfo(blob);
        if (StringUtils.isNotBlank(outputVariable)) {
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> result = mapper.convertValue(info, new TypeReference<>(){});
            ctx.put(outputVariable, result);
        }
        if (StringUtils.isNotBlank(outputVariableJsonStr)) {
            ctx.put(outputVariableJsonStr, info.toString());
        }
        return blob;
    }
}