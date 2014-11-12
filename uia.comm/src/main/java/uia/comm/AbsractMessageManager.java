package uia.comm;

public abstract class AbsractMessageManager implements MessageManager {

	@Override
	public byte[] decode(byte[] data) {
		return data;
	}

	@Override
	public byte[] encode(byte[] data) {
		return data;
	}

}
