UIA Comm
================

[![Download](https://api.bintray.com/packages/uia4j/maven/uia-comm/images/download.svg)](https://bintray.com/uia4j/maven/uia-comm/_latestVersion)
[![Build Status](https://travis-ci.org/uia4j/uia-comm.svg?branch=master)](https://travis-ci.org/uia4j/uia-comm)
[![codecov](https://codecov.io/gh/uia4j/uia-comm/branch/master/graph/badge.svg)](https://codecov.io/gh/uia4j/uia-comm)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/9766faacb361423b9b6e8e95bf3024d6)](https://www.codacy.com/app/gazer2kanlin/uia-comm?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=uia4j/uia-comm&amp;utm_campaign=Badge_Grade)
[![License](https://img.shields.io/github/license/uia4j/uia-comm.svg)](LICENSE)

Socket client, server & RS232 library.

## Android 支援

* SocketClient - 已測試

* SocketServer - 進行中

* RS232 - __不支援__

## 關鍵資訊

### Protocol
Provide 介面用於定義資料結構與追縱收到的資料。
* uia.comm.protocol.Protocol
    定義資料結構

* uia.comm.protocol.ProtocolMonitor
    追蹤收到的資料

### 預設實作
* HLProtocol - Head-Length
    ```java
    public HLProtocol(
        int lenStartOffset, // 主資料首字元與完整資料首字元的偏移量。
        int lenEndOffset,   // 主資料尾字元與完整資料尾字元的偏移量。
        int lenFieldIdx,    // 長度欄位開始位元位置。
        int lenFieldCount,  // 長度欄位位元數。
        LenReader reader,   // 長度欄位讀取介面
        byte[] head)        // 完整資料開始的位元組值。
    ```
* HOProtocol - Head Only
    ```java
    public HOProtocol(
        byte[] head,        // 完整資料開始的位元組值。
        int maxLength) {    // 完整資料最常長度。
    }
    ```
* HTProtocol - Head-Tail
    ```java
    public HTProtocol(
        byte[] head,        // 完整資料開始的位元組值。
        byte[] tail) {      // 完整資料結束的位元組值。
    }
    ```

* HTxProtocol - Head-Tail sepcial
    ```java
    public HTxProtocol(
        byte head,          // 完整資料開始的位元值。
        int hc,             // 開始的位元值重複次數。
        byte tail) {        // 完整資料結束的位元值。
    }
    ```

* NGProtocol - Not Good
    ```java
    public NGProtocol() {
    }                       // 無結構描述。
    ```

* XMLProtocol - XML
    ```java
    public XMLProtocol(     
        String rootTag) {   // XML 根標籤。
    }
    ```

### Message Manager
提供方法進行控制資料處理流程。
* uia.comm.MessageManager

### Call In/Out
提供介面處理資料進出。
* uia.comm.MessageCallIn
    處理來自遠端的 __請求(Request)__。
    來自遠端的 __回應(Response)__ 會被配對到 __近端發出的請求(Request)__。

* uia.comm.MessageCallout
    處理近端發出的 __請求(Reqeust)__。
    請求(Request) 對應的 __回應(Response)__ 會自動被配對上。


## Maven
```xml
<dependency>
    <groupId>org.uia.solution</groupId>
    <artifactId>uia-comm</artifactId>
    <version>0.3.3</version>
</dependency>
```
### 相依套件

* [uia-utils](https://github.com/uia4j/uia-utils) - UIA common utilities

## 版權與授權

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

[http://www.apache.org/licenses/LICENSE-2.0](http://www.apache.org/licenses/LICENSE-2.0)

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
