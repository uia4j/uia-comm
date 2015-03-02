/*******************************************************************************
 * * Copyright (c) 2015, UIA
 * * All rights reserved.
 * * Redistribution and use in source and binary forms, with or without
 * * modification, are permitted provided that the following conditions are met:
 * *
 * *     * Redistributions of source code must retain the above copyright
 * *       notice, this list of conditions and the following disclaimer.
 * *     * Redistributions in binary form must reproduce the above copyright
 * *       notice, this list of conditions and the following disclaimer in the
 * *       documentation and/or other materials provided with the distribution.
 * *     * Neither the name of the {company name} nor the
 * *       names of its contributors may be used to endorse or promote products
 * *       derived from this software without specific prior written permission.
 * *
 * * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS "AS IS" AND ANY
 * * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * * DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 * * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/
package uia.comm.protocol.hl;

import uia.comm.protocol.AbstractProtocolMonitor;
import uia.comm.protocol.ProtocolEventArgs;

public class HLProtocolMonitor<T> extends AbstractProtocolMonitor<T> {

	int headIdx;

	final HLProtocol<T> protocol;

	private HLState<T> state;

	public HLProtocolMonitor(String name, HLProtocol<T> protocol) {
		super(name);

		this.protocol = protocol;
		this.state = new IdleState<T>();
	}

	public HLState<T> getState()
	{
		return this.state;
	}

	public void setState(HLState<T> state) {
		this.state = state;
	}

	@Override
	public void read(byte one) {
		this.state.accept(this, one);
	}

	@Override
	public void reset() {
		this.headIdx = 0;
		this.state = new IdleState<T>();
		this.data.clear();
	}

	int readLenFromLeField() {
		byte[] data = new byte[this.protocol.lenFieldByteCount];
		for (int i = 0; i < data.length; i++)
		{
			data[i] = this.data.get(this.protocol.lenFieldStartIdx + i);
		}

		return this.protocol.reader.read(data);
	}

	void addOne(byte one) {
		this.data.add(one);
	}

	void cancelPacking(ProtocolEventArgs.ErrorCode errorCode)
	{
		ProtocolEventArgs args = new ProtocolEventArgs(packing(), errorCode);
		this.data.clear();
		this.protocol.raiseBorken(this, args);
	}

	void finishPacking()
	{
		ProtocolEventArgs args = new ProtocolEventArgs(packing());
		this.data.clear();
		this.protocol.raiseMessageReceived(this, args);
	}
}
