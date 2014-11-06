package uia.comm.protocol.ht;

public interface HTState<C> {

	public void accept(HTProtocolMonitor<C> monitor, byte one);

}
