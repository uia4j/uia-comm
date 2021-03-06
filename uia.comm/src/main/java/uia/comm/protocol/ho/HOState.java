/*******************************************************************************
 * Copyright 2017 UIA
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package uia.comm.protocol.ho;

/**
 *
 * @author Kyle K. Lin
 *
 * @param <T> Reference.
 */
public interface HOState<T> {

    /**
     * handle one byte from data channel.
     *
     * @param monitor one which monitors data channel.
     * @param one data.
     */
    public void accept(HOProtocolMonitor<T> monitor, byte one);

    /**
     * handle when no data in data channel
     *
     * @param monitor one which monitors data channel.
     */
    public void end(HOProtocolMonitor<T> monitor);
}
