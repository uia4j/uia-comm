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
package uia.comm;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

/**
 * 
 * @author Kyle
 * 
 */
public class SocketDataSelector {

	private boolean started;

	private final Selector selector;

	/**
	 * 
	 * @throws IOException
	 */
	public SocketDataSelector() throws IOException {
		this.selector = Selector.open();
	}

	/**
	 * 
	 * @param ch
	 * @param controller
	 * @throws ClosedChannelException
	 */
	public void register(SocketChannel ch, SocketDataController controller) throws ClosedChannelException {
		ch.register(this.selector, SelectionKey.OP_READ, controller);
	}

	/**
	 * 
	 */
	public void start() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				running();
			}

		}).start();
	}

	/**
	 * 
	 */
	public void stop() {
		this.started = false;
		this.selector.wakeup();
	}

	private void running() {
		while (this.started) {
			try {
				this.selector.select();
			} catch (Exception ex) {
			}

			Iterator<SelectionKey> iter = this.selector.selectedKeys().iterator();
			while (iter.hasNext()) {
				SelectionKey key = iter.next();
				iter.remove();

				if (key.isReadable()) {
					SocketDataController controller = (SocketDataController) key.attachment();
					try {
						controller.receive();
					} catch (IOException e) {
						// TODO: raise event to disconnect
					}
				}
			}
		}
	}
}
