package roomescape.client;

/**
 * 응답을 읽는 단계(read)에서 실패한 경우. 요청은 토스에 전달되었으나 응답을 받지 못해 승인 여부를 알 수 없다.
 * 실패로 단정해서는 안 되며, 결과 확인·재시도가 가능하도록 안내해야 한다.
 */
public class TossConfirmResultUnknownException extends RuntimeException {

    public TossConfirmResultUnknownException(Throwable cause) {
        super("결제 승인 결과를 확인하지 못했습니다. 결제가 완료되었을 수 있으니 예약 내역을 확인해주세요.", cause);
    }
}
