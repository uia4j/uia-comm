package uia.comm.protocol.hl;

public interface HLState<T> {

	public void accept(HLProtocolMonitor<T> monitor, byte one);
}
