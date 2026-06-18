package roomescape.client;

/**
 * 연결 단계(connect)에서 실패한 경우. 요청이 토스에 도달하지 못했으므로 결제는 확실히 처리되지 않았다.
 */
public class TossConnectionException extends RuntimeException {

    public TossConnectionException(Throwable cause) {
        super("결제 서버에 연결할 수 없습니다. 잠시 후 다시 시도해주세요.", cause);
    }
}
