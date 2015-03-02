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

import uia.comm.protocol.ProtocolEventArgs;

public class HeadState<T> implements HLState<T> {

	@Override
	public void accept(HLProtocolMonitor<T> monitor, byte one) {
		monitor.addOne(one);
		if (monitor.headIdx < monitor.protocol.head.length && one == monitor.protocol.head[monitor.headIdx]) {
			monitor.headIdx++;
			if (monitor.headIdx >= monitor.protocol.head.length) {
				monitor.headIdx = 0;
				monitor.setState(new BodyState<T>());
			}
		} else {
			int idx = monitor.headIdx;
			monitor.cancelPacking(ProtocolEventArgs.ErrorCode.ERR_HEAD);
			monitor.reset();
			if (idx > 0)
			{
				monitor.setState(new HeadState<T>());
				monitor.read(one);
			}
			else
			{
				monitor.setState(new IdleState<T>());
			}
		}
	}

}
