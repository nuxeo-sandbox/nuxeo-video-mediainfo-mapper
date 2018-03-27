/*
 * (C) Copyright 2016 Nuxeo SA (http://nuxeo.com/) and others.
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
 */
package org.nuxeo.labs.video.mediainfo.mapper;

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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class MediaInfoHelper {

    public static final String MEDIAINFO_INFO_COMMAND_LINE = "mediainfo-info";

    // Utility class.
    private MediaInfoHelper() {
    }

    // Calls media info on the input video and returns the output processed in a
    // map of maps (following mediaInfo output pattern).
    public static Map<String, Map<String, String>> getProcessedMediaInfo(Blob video) throws NuxeoException {
        return processMediaInfo(getRawMediaInfo(video));
    }

    /*
     * Get one specific information on the input blob video using mediaInfo. a call to
     * getSpecificMediaInfo("Video","Width",MyVideo) will get something like 512 pixels
     */
    public static String getSpecificMediaInfo(String key1, String key2, Blob video) throws NuxeoException {
        return getProcessedMediaInfo(video).get(key1).get(key2);
    }

    // Get the String List from mediainfo without any processing
    public static List<String> getRawMediaInfo(Blob blob) throws NuxeoException {
        String uriStr = null;
        if (blob instanceof ManagedBlob) {
            ManagedBlob managedBlob = (ManagedBlob) blob;
            BlobProvider blobProvider = Framework.getLocalService(BlobManager.class).getBlobProvider(blob);
            try {
                URI uri = blobProvider.getURI(managedBlob, BlobManager.UsageHint.DOWNLOAD,null);
                if (uri != null) uriStr = uri.toString();
            } catch (IOException e) {
                // continue
            }
        }

        File file = null;
        if (uriStr == null) {
            try {
                file = File.createTempFile("mediainfo", "." + FilenameUtils.getExtension(blob.getDigest()));
                blob.transferTo(file);
                uriStr = file.getAbsolutePath();
            } catch (IOException e) {
                throw new NuxeoException(e);
            }
        }

        try {
            CommandLineExecutorService cleService = Framework.getLocalService(CommandLineExecutorService.class);
            CmdParameters params = new CmdParameters();
            params.addNamedParameter("url", uriStr);
            return cleService.execCommand(MEDIAINFO_INFO_COMMAND_LINE, params).getOutput();
        } catch (CommandNotAvailable e) {
            throw new NuxeoException(e);
        } finally {
            if (file != null) {
                file.delete();
            }
        }
    }

    /*
     * Processes the raw String List from media info and returns a map of maps. Result of media info is a list of String
     * that follows a specific pattern: The result of mediainfo looks like : General Format : AVI Codec ID : ISOM Video
     * FORMAT/INFO : AVC Codexc ID : avc1 This method returns a map of maps that follow this pattern for example General
     * is the first Key and references a map with Format as a key and AVI as a value.
     */
    public static Map<String, Map<String, String>> processMediaInfo(List<String> input) {

        Map<String, Map<String, String>> output = new HashMap<>();
        int nextIndex = input.indexOf("");
        List<String> remainingList = input;
        List<String> subList;

        while (nextIndex != -1 && !remainingList.get(0).equals("")) {
            subList = remainingList.subList(1, nextIndex);
            output.put(remainingList.get(0), processSubList(subList));
            if (nextIndex == remainingList.size() - 1)
                break;
            remainingList = remainingList.subList(nextIndex + 1, remainingList.size());
            nextIndex = remainingList.indexOf("");
        }
        return output;
    }

    // Returns a map from a String List containing a key and a value in each
    // line separated by a ":"
    protected static Map<String, String> processSubList(List<String> subList) {
        Map<String, String> subMap = new HashMap<>();
        Iterator<String> subListIterator = subList.iterator();
        String singleInfoLine;
        while (subListIterator.hasNext()) {
            singleInfoLine = subListIterator.next();
            if (singleInfoLine.length() >= 3 && singleInfoLine.indexOf(":")>=1) {
                subMap.put(getSingleInfoKey(singleInfoLine), getSingleInfoValue(singleInfoLine));
            }
        }
        return subMap;
    }

    // get the first part of a single entry line from the output String list :
    // before ":". Removes useless white spaces
    protected static String getSingleInfoKey(String input) {
        return input.substring(0, input.indexOf(":")).trim();
    }

    // get the second part of a single entry line from the output String list :
    // after ":" Removes useless white spaces
    protected static String getSingleInfoValue(String input) {
        return input.substring(input.indexOf(":") + 1).trim();
    }

}
