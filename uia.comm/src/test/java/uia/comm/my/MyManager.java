package uia.comm.my;

import java.util.Arrays;

import uia.comm.MessageManager;

public class MyManager implements MessageManager {

	@Override
	public boolean isCallIn(String cmd) {
		return "ABC".equals(cmd);
	}

	@Override
	public String findCmd(byte[] data) {
		return new String(Arrays.copyOfRange(data, 1, 4));
	}

	@Override
	public String findTx(byte[] data) {
		return new String(Arrays.copyOfRange(data, 4, 5));
	}

}
