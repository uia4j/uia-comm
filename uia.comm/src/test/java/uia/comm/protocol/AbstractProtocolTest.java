package uia.comm.protocol;

public class AbstractProtocolTest implements ProtocolEventHandler<Object> {

	protected ProtocolEventArgs recvArgs;
	
	protected ProtocolEventArgs errArgs;

	@Override
	public void messageReceived(ProtocolMonitor<Object> monitor,
			ProtocolEventArgs args) {
		this.recvArgs = args;
		this.errArgs = null;
		
	}

	@Override
	public void messageError(ProtocolMonitor<Object> monitor,
			ProtocolEventArgs args) {
		this.recvArgs = null;
		this.errArgs = args;
	}

}
