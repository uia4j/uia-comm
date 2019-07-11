UIA Comm
================

[![Download](https://api.bintray.com/packages/uia4j/maven/uia-comm/images/download.svg)](https://bintray.com/uia4j/maven/uia-comm/_latestVersion)
[![Build Status](https://travis-ci.org/uia4j/uia-comm.svg?branch=master)](https://travis-ci.org/uia4j/uia-comm)
[![codecov](https://codecov.io/gh/uia4j/uia-comm/branch/master/graph/badge.svg)](https://codecov.io/gh/uia4j/uia-comm)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/9766faacb361423b9b6e8e95bf3024d6)](https://www.codacy.com/app/gazer2kanlin/uia-comm?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=uia4j/uia-comm&amp;utm_campaign=Badge_Grade)
[![License](https://img.shields.io/github/license/uia4j/uia-comm.svg)](LICENSE)

[![java profiler](https://www.ej-technologies.com/images/product_banners/jprofiler_small.png)](https://www.ej-technologies.com/products/jprofiler/overview.html)

Socket client, server & RS232 library.


[README Chinese](README_TW.md)

## Android Support

* SocketClient - tested

* SocketServer - working

* RS232 - __NOT SUPPORT__

## Key Abstraction

### Protocol
Provide interfaces to define data structure and handle incoming data.
* uia.comm.protocol.Protocol - define data structure

* uia.comm.protocol.ProtocolMonitor - handle incoming data.

### Default Implementation
* HLProtocol - Head-Length
    ```java
    public HLProtocol(
        int lenStartOffset, // The offset of first byte of primary data from first byte of full data.
        int lenEndOffset,   // The offset of last byte of primary data from last byte of full data
        int lenFieldIdx,    // First byte index of LENGTH field.
        int lenFieldCount,  // Byte count of LENGTH field.
        LenReader reader,   // Reader of LENGTH field.
        byte[] head)        // Starting value of full data.
    ```
* HOProtocol - Head Only
    ```java
    public HOProtocol(
        byte[] head,        // Starting value of full data.
        int maxLength) {    // Max. length.
    }
    ```
* HTProtocol - Head-Tail
    ```java
    public HTProtocol(
        byte[] head,        // Starting value of full data.
        byte[] tail) {      // End value of full data.
    }
    ```

* HTxProtocol - Head-Tail sepcial
    ```java
    public HTxProtocol(
        byte head,          // Starting value of full data.
        int hc,             // Repeat count of head.
        byte tail) {        // End byte value of full data.
    }
    ```

* NGProtocol - Not Good
    ```java
    public NGProtocol() {
    }                       // Structrueless.
    ```

* XMLProtocol - XML
    ```java
    public XMLProtocol(     
        String rootTag) {   // XML root tag.
    }
    ```

### Message Manager
Provide core functions for control flow of messages.
* uia.comm.MessageManager

### Call In/Out
Provoide interfaces to handle incoming and outgoing messages.
* uia.comm.MessageCallIn
    * Handle __Request__ from remote side.
    * __Response__ from the remote side will be paired to __Callout Request__.

* uia.comm.MessageCallout
    * Handle __Reqeust__ form client side.
    * __Response__ of the Request will be paired automatically.

## Maven
```xml
<dependency>
    <groupId>org.uia.solution</groupId>
    <artifactId>uia-comm</artifactId>
    <version>0.3.3</version>
</dependency>
```
### Dependency Libraries

* [uia-utils](https://github.com/uia4j/uia-utils) - UIA common utilities

## Copyright and License

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

[http://www.apache.org/licenses/LICENSE-2.0](http://www.apache.org/licenses/LICENSE-2.0)

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
