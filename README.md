## Description
This plugin provides a listener and async workers to extract binary metadata from video files using mediainfo.

[![Build Status](https://qa.nuxeo.org/jenkins/buildStatus/icon?job=Sandbox/sandbox_nuxeo-video-mediainfo-mapper-master)](https://qa.nuxeo.org/jenkins/job/Sandbox/sandbox_nuxeo-video-mediainfo-mapper-master)

## Using the Plugin

The mapping between fields extracted from `mediainfo` is done in an asynchronous worker, using a JavaScript automation callback, named `javascript.MediaInfoMapping` (can be overriden).

It uses an operaiton provided by the plugin: `Blob.ExtractMediaMetadata`. This operation receives a blob as input and accepts 2 optional parameters: `outputVariable` and `outputVariableJsonStr`.  Both are strings, and the names of the context variable in which the result will be saved. You can pass one or the other (but both can't be not passed).

`outputVariable` receives a Java `Map`. `outputVariableJsonStr`  receives the same, as a JSON string.



## How to build
```
git clone https://github.com/nuxeo-sandbox/nuxeo-video-mediainfo-mapper
cd nuxeo-video-mediainfo-mapper
mvn clean install
```

## Deploying
- Install the marketplace package from the admin center or using nuxeoctl

## Configuration
The default mapping can be changed in Nuxeo Studio by overriding the `javascript.MediaInfoMapping` automation script.

## Known limitations
**These features are not part of the Nuxeo Production platform.**

These solutions are provided for inspiration and we encourage customers to use them as code samples and learning resources.

This is a moving project (no API maintenance, no deprecation process, etc.) If any of these solutions are found to be useful for the Nuxeo Platform in general, they will be integrated directly into platform, not maintained here.

## About Nuxeo
Nuxeo dramatically improves how content-based applications are built, managed and deployed, making customers more agile, innovative and successful. Nuxeo provides a next generation, enterprise ready platform for building traditional and cutting-edge content oriented applications. Combining a powerful application development environment with SaaS-based tools and a modular architecture, the Nuxeo Platform and Products provide clear business value to some of the most recognizable brands including Verizon, Electronic Arts, Sharp, FICO, the U.S. Navy, and Boeing. Nuxeo is headquartered in New York and Paris. More information is available at [www.nuxeo.com](http://www.nuxeo.com).
