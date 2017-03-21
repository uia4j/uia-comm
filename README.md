UIA Comm
================

[![Build Status](https://travis-ci.org/gazer2kanlin/uia.comm4j.svg?branch=0.2.0.0)](https://travis-ci.org/gazer2kanlin/uia.comm4j)
[![Codecov](https://img.shields.io/codecov/c/github/gazer2kanlin/uia.comm4j.svg)](https://codecov.io/gh/gazer2kanlin/uia.comm4j)
[![License](https://img.shields.io/github/license/gazer2kanlin/uia.comm4j.svg)](LICENSE)

Socket client, server & RS232 library.

## Android Support

* SocketClient - tested

* SocketServer - working

* RS232 - __NOT SUPPORT__


## Key Abstraction

### Protocol
Provide protocol interface to define structure abstraction.

### Message Manager
Provide message manager to verify and validate I/O message.

### Call In/Out
Define interface to handle I/O message.

## Maven
Because uia.comm uses [uia.utils](https://github.com/gazer2kanlin/uia.utils4j) deployed on jcenter, configure local Maven __settings.xml__ first.

settings.xml in .m2 directory:
```
<profiles>
    <profile>
        <repositories>
            <repository>
                <snapshots>
                    <enabled>false</enabled>
                </snapshots>
                <id>central</id>
                <name>bintray</name>
                <url>http://jcenter.bintray.com</url>
            </repository>
        </repositories>
        <pluginRepositories>
            <pluginRepository>
                <snapshots>
                    <enabled>false</enabled>
                </snapshots>
                <id>central</id>
                <name>bintray-plugins</name>
                <url>http://jcenter.bintray.com</url>
            </pluginRepository>
        </pluginRepositories>
        <id>bintray</id>
    </profile>
</profiles>
<activeProfiles>
    <activeProfile>bintray</activeProfile>
</activeProfiles>
```
pom.xml in your project:
```
<dependency>
    <groupId>uia</groupId>
    <artifactId>uia.comm</artifactId>
    <version>0.2.1</version>
</dependency>
```

## Dependency Libraries

* [uia.utils](https://github.com/gazer2kanlin/uia.utils4j) - UIA common utilities

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
