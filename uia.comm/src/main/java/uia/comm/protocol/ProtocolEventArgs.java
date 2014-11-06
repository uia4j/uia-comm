package uia.comm.protocol;

public class ProtocolEventArgs {

	public enum ErrorCode {
		OK,
		ERR_HEAD,
		ERR_TAIL,
		ERR_BODY,
		ERR_BODY_LENGTH,
		ERR_CHKSUM,
		ERR_TIMEOUT,
		ERR_OTHER
	}

	private final byte[] data;

	private final ErrorCode errorCode;

	public ProtocolEventArgs(final byte[] data) {
		this.data = data;
		this.errorCode = ErrorCode.OK;
	}

	public ProtocolEventArgs(final byte[] data, final ErrorCode errorCode) {
		this.data = data;
		this.errorCode = errorCode;
	}

	public byte[] getData() {
		return this.data;
	}

	public ErrorCode getErrorCode() {
		return this.errorCode;
	}
}
