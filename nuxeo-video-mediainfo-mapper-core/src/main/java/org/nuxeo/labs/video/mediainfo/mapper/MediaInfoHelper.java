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
 *     Michael Vachette
 */
package org.nuxeo.labs.video.mediainfo.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FilenameUtils;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.NuxeoException;
import org.nuxeo.ecm.core.blob.BlobManager;
import org.nuxeo.ecm.core.blob.BlobProvider;
import org.nuxeo.ecm.core.blob.ManagedBlob;
import org.nuxeo.ecm.platform.commandline.executor.api.CmdParameters;
import org.nuxeo.ecm.platform.commandline.executor.api.CommandLineExecutorService;
import org.nuxeo.ecm.platform.commandline.executor.api.CommandNotAvailable;
import org.nuxeo.runtime.api.Framework;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.List;

public class MediaInfoHelper {

    public static final String MEDIAINFO_INFO_COMMAND_LINE = "mediainfo-info";

    // Utility class.
    private MediaInfoHelper() {
    }

    // Calls media info on the input video and returns the output processed in a
    // map of maps (following mediaInfo output pattern).
    public static JsonNode getProcessedMediaInfo(Blob video) throws NuxeoException, JsonProcessingException {
        return processMediaInfo(getRawMediaInfo(video));
    }

    // Get the String List from mediainfo without any processing
    public static List<String> getRawMediaInfo(Blob blob) throws NuxeoException {
        String uriStr = null;
        if (blob instanceof ManagedBlob) {
            ManagedBlob managedBlob = (ManagedBlob) blob;
            BlobProvider blobProvider = Framework.getService(BlobManager.class).getBlobProvider(blob);
            try {
                URI uri = blobProvider.getURI(managedBlob, BlobManager.UsageHint.DOWNLOAD,null);
                if (uri != null) uriStr = uri.toString();
            } catch (IOException e) {
                // continue
            }
        }

        if (uriStr == null) {
            try {
                File file = Framework.createTempFile("mediainfo", "." + FilenameUtils.getExtension(blob.getFilename()));
                blob.transferTo(file);
                uriStr = file.getAbsolutePath();
            } catch (IOException e) {
                throw new NuxeoException(e);
            }
        }

        try {
            CommandLineExecutorService cleService = Framework.getService(CommandLineExecutorService.class);
            CmdParameters params = new CmdParameters();
            params.addNamedParameter("url", uriStr);
            return cleService.execCommand(MEDIAINFO_INFO_COMMAND_LINE, params).getOutput();
        } catch (CommandNotAvailable e) {
            throw new NuxeoException(e);
        }
    }

    public static JsonNode processMediaInfo(List<String> input) throws JsonProcessingException {
        int start = input.indexOf("{");
        int end = input.lastIndexOf("}");
        String usefulInput = String.join("",input.subList(start,end+1));
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readTree(usefulInput);
    }

}
