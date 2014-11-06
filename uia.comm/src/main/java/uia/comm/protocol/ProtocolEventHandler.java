package uia.comm.protocol;

public interface ProtocolEventHandler<C> {

	public void messageReceived(ProtocolMonitor<C> monitor, ProtocolEventArgs args);

	public void messageError(ProtocolMonitor<C> monitor, ProtocolEventArgs args);

}
