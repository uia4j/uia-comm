package uia.comm;

import java.util.concurrent.Callable;

class MessageCallOutConcurrent implements MessageCallOut, Callable<byte[]> {

    private final String txId;

    private final long timeout;

    private byte[] result;

    private int state;  // 0: execute, 1: handled, -1: timeout.

    MessageCallOutConcurrent(String txId, long timeout) {
        this.txId = txId;
        this.timeout = timeout;
        this.state = 0;
    }

    @Override
    public byte[] call() throws Exception {
        synchronized (this.txId) {
            if (this.state != 0) {
                return this.result;
            }

            this.txId.wait(this.timeout + 20);
            if (this.state == 0) {
                this.state = -1;
            }
        }
        return this.result;
    }

    @Override
    public String getTxId() {
        return this.txId;
    }

    @Override
    public void execute(byte[] reply) {
        synchronized (this.txId) {
            if (this.state == 0) {
                this.state = 1;
                this.result = reply;
            }
            this.txId.notifyAll();
        }
    }

    @Override
    public void timeout() {
        synchronized (this.txId) {
            if (this.state == 0) {
                this.state = -1;
            }
            this.txId.notifyAll();
        }
    }
}
