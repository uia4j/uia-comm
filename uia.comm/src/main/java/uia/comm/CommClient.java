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
package uia.comm;

import java.net.SocketException;

import uia.comm.protocol.Protocol;

/**
 *
 * @author Kyle
 *
 * @param <C>
 */
public interface CommClient<C> {

    /**
     * Get name.
     *
     * @return The name.
     */
    public abstract String getName();

    /**
     * Is connected or not.
     * @return Connect or not.
     */
    public abstract boolean isConnected();

    /**
     * Disconnect.
     */
    public abstract void disconnect();

    /**
     * Get protocol on this socket channel.
     *
     * @return The protocol.
     */
    public abstract Protocol<C> getProtocol();

    /**
     * Register call in worker.
     *
     * @param callIn The call in worker.
     */
    public abstract void registerCallin(MessageCallIn<C> callIn);

    /**
     * Send data to socket server.
     *
     * @param data Data.
     * @return Send result.
     * @throws SocketException Raise when server is not connected.
     */
    public abstract boolean send(final byte[] data) throws SocketException;

    /**
     * Send data to socket server.
     *
     * @param data Data.
     * @param times Retry times.
     * @return Send result.
     * @throws SocketException Raise when server is not connected.
     */
    public abstract boolean send(final byte[] data, int times) throws SocketException;

    /**
     * send data to socket server and wait result.
     *
     * @param data Data.
     * @param txId Transaction id.
     * @param timeout Timeout milliseconds.
     * @return Reply data or Null if timeout.
     * @throws SocketException Raise when server is not connected or send to server failure.
     */
    public abstract byte[] send(final byte[] data, String txId, long timeout) throws SocketException;

    /**
     * send data to socket server and wait result.
     *
     * @param data Data.
     * @param txId Transaction id.
     * @param timeout Timeout milliseconds.
     * @param retry Retry times.
     * @return Reply data or Null if timeout.
     * @throws SocketException Raise when server is not connected or send to server failure.
     */
    public abstract byte[] send(final byte[] data, String txId, long timeout, int retry) throws SocketException;

    /**
     * Send data to socket server.
     *
     * @param data Data.
     * @param callOut Reply message worker.
     * @param timeout Timeout seconds.
     * @return Send result.
     * @throws SocketException Raise if not started.
     */
    public abstract boolean send(final byte[] data, final MessageCallOut callOut, long timeout) throws SocketException;

    /**
     * Send data to socket server.
     *
     * @param data Data.
     * @param callOut Reply message worker.
     * @param timeout Timeout seconds.
     * @param retry Retry times.
     * @return Send result.
     * @throws SocketException Raise if not started.
     */
    public abstract boolean send(final byte[] data, final MessageCallOut callOut, long timeout, int retry) throws SocketException;
}
