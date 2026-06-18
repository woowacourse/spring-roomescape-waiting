package roomescape.payment.infra.client.exception;

/**
 * 나가는 호출이 자체 한도를 넘어, 외부로 보내지 않고 거부했음을 알리는 예외.
 */
public class OutboundRateLimitException extends RuntimeException {

  public OutboundRateLimitException(String message) {
    super(message);
  }

}
