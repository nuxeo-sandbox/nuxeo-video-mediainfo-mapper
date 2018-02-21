package org.nuxeo.labs.video.mediainfo.mapper;

import java.io.Serializable;
import java.util.Map;

public class VideoInfoTestHelper {

    public static boolean isVideoInfoCorrect(Map<String,Serializable> videoInfo) {
        return videoInfo!=null &&
          (double)videoInfo.get("duration") == 622.34d &&
          (long)videoInfo.get("width") == 176l &&
          (long)videoInfo.get("height") == 144l &&
          "MPEG-4".equals(videoInfo.get("format")) &&
          (double)videoInfo.get("frameRate") ==9.871d;
    }

}
