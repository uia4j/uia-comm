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
package uia.comm.protocol;

/**
 *
 * @author Kyle K. Lin
 *
 * @param <C>
 */
public interface ProtocolMonitor<C> {

    /**
     * Get data count in this monitor.
     * @return Data count.
     */
    public int getDataLength();

    /**
     * Get protocol which creates this monitor.
     *
     * @return The protocol.
     */
    public Protocol<C> getProtocol();

    /**
     * Get the name.
     *
     * @return The name.
     */
    public String getName();

    /**
     * Read a byte from input source.
     *
     * @param one One byte.
     */
    public void read(byte one);

    /**
     * call when no data in data channel.
     */
    public void readEnd();

    /**
     * reset monitor to idle state.
     */
    public void reset();

    /**
     * Get the controller of the monitor.
     *
     * @return The controller.
     */
    public C getController();

    /**
     * Set the controller of ths monitor.
     *
     * @param controller The controller.
     */
    public void setController(C controller);

    /**
     * If monitor is running or idle.
     * @return Running or not.
     */
    public boolean isRunning();

    /**
     * Get state.
     * @return State.
     */
    public String getStateInfo();
}
