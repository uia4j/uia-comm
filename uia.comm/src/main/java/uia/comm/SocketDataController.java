/*******************************************************************************
 * * Copyright (c) 2015, UIA * All rights reserved. * Redistribution and use in source and binary forms, with or without * modification, are permitted provided that the following conditions are met: * * * Redistributions of source code must retain
 * the above copyright * notice, this list of conditions and the following disclaimer. * * Redistributions in binary form must reproduce the above copyright * notice, this list of conditions and the following disclaimer in the * documentation and/or
 * other materials provided with the distribution. * * Neither the name of the {company name} nor the * names of its contributors may be used to endorse or promote products * derived from this software without specific prior written permission. * *
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS "AS IS" AND ANY * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE * DISCLAIMED. IN NO
 * EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; * LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS * SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/
package uia.comm;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;

import uia.comm.protocol.ProtocolMonitor;

/**
 * 
 * @author Kyle
 * 
 */
public class SocketDataController {

    private final static Logger logger = Logger.getLogger(SocketDataController.class);

    private final String name;;

    private boolean started;

    private final Selector selector;

    private final ProtocolMonitor<SocketDataController> monitor;

    private SocketApp app;

    private SocketChannel ch;

    /**
     * 
     * @param name Name.
     * @param app Socket client or server.
     * @param ch Socket channel used to receive and send message.
     * @param monitor Monitor used to handle received message.
     * @param idlePeriod
     * @throws IOException
     */
    SocketDataController(
            String name,
            SocketApp app,
            SocketChannel ch,
            ProtocolMonitor<SocketDataController> monitor) throws IOException {
        this.name = name;
        this.app = app;
        this.started = false;
        this.selector = Selector.open();
        this.ch = ch;
        this.ch.configureBlocking(false);
        this.ch.register(this.selector, SelectionKey.OP_READ);
        this.monitor = monitor;
        this.monitor.setController(this);
    }

    /**
     * Get name.
     * @return Name of this controller.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Send data to remote.
     * 
     * @param data Data.
     * @param times Retry times.
     * @return Success or not.
     */
    public synchronized boolean send(byte[] data, int times) {
        while (times > 0) {
            try {
                this.ch.socket().setSendBufferSize(data.length);
                int cnt = this.ch.write(ByteBuffer.wrap(data));
                if (cnt == data.length) {
                    return true;
                }
                else {
                    logger.fatal("write count error!!");
                }
            }
            catch (Exception ex) {

            }
            finally {
                times--;
            }
        }
        return false;
    }

    /**
     * Start this controller using internal selector.
     * 
     * @return true if start success first time.
     */
    synchronized boolean start() {
        if (this.ch == null || this.started) {
            return false;
        }
        this.started = true;
        new Thread(new Runnable() {

            @Override
            public void run() {
                running();
            }

        }).start();
        return true;
    }

    /**
     * Stop this controller.
     */
    synchronized void stop() {
        if (this.ch != null) {
            try {
                this.ch.close();
                this.ch.keyFor(this.selector).cancel();
            }
            catch (Exception ex) {

            }
        }
        this.ch = null;
        this.started = false;
    }

    /**
     * Receive message from socket channel.
     * 
     * @throws IOException
     */
    synchronized void receive() throws IOException {
        if (this.ch == null) {
            return;
        }

        int len = 0;
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        do {
            len = this.ch.read(buffer);
            if (len > 0) {
                byte[] value = (byte[]) buffer.flip().array();
                value = Arrays.copyOf(value, len);
                for (byte b : value) {
                    this.monitor.read(b);
                }
            }
            buffer.clear();
        }
        while (len > 0);
        this.monitor.readEnd();
    }

    SocketChannel getChannel() {
        return this.ch;
    }

    private void idleOut() {
        if (this.app != null) {
            this.app.idle(this);
        }
    }

    private void running() {
        // use internal selector to handle received data.
        while (this.started) {
            try {
                this.selector.select(); // wait NIO event
            }
            catch (Exception ex) {
                continue;
            }

            Iterator<SelectionKey> iterator = this.selector.selectedKeys().iterator();
            while (iterator.hasNext()) {
                SelectionKey selectionKey = iterator.next();
                @SuppressWarnings("unused")
                SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
                iterator.remove();

                try {
                    receive();
                }
                catch (IOException e) {

                }
            }
        }
    }

    class IdleTimer extends TimerTask {

        private Timer timer;

        private boolean stopped;

        private IdleTimer(int period) {
            this.timer = new Timer();
            this.timer.schedule(this, period);
        }

        public void stop() {
            this.stopped = true;
            this.timer.cancel();
            this.timer.purge();
        }

        @Override
        public void run() {
            if (!this.stopped) {
                this.stopped = true;
                this.timer.cancel();
                this.timer.purge();
                idleOut();
            }
        }
    }
}
