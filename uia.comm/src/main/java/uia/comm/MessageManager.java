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

/**
 *
 * @author Kyle K. Lin
 *
 */
public interface MessageManager {

    /**
     * Check if this is a CallIn command.
     *
     * @param cmd Command name.
     * @return True if it is a CallIn command.
     */
    public boolean isCallIn(String cmd);

    /**
     * Find name of command from data.
     *
     * @param data Data.
     * @return Command name.
     */
    public String findCmd(byte[] data);

    /**
     * Find transaction id from data.
     *
     * @param data Data.
     * @return Transaction id.
     */
    public String findTx(byte[] data);

    /**
     * Decode data to domain format.
     * @param data Original data.
     * @return Result.
     */
    public byte[] decode(byte[] data);

    /**
     * Encode domain data to byte array.
     * @param data Domain data.
     * @return Result.
     */
    public byte[] encode(byte[] data);

    /**
     * Validate data if correct or not.
     * @param data Data need to be validated.
     * @return result.
     */
    public boolean validate(byte[] data);

}
