package roomescape.exception;

public class NotFoundException extends RoomescapeException {

    private static final String NOT_FOUND_FORMAT = "ID %d번 %s 정보를 찾을 수 없습니다.";

    private NotFoundException(String message) {
        super(message);
    }

    public static NotFoundException reservation(Long id) {
        return new NotFoundException(NOT_FOUND_FORMAT.formatted(id, "예약"));
    }

    public static NotFoundException reservationWaiting(Long id) {
        return new NotFoundException(NOT_FOUND_FORMAT.formatted(id, "예약 대기"));
    }

    public static NotFoundException reservationTime(Long id) {
        return new NotFoundException(NOT_FOUND_FORMAT.formatted(id, "예약 시간"));
    }

    public static NotFoundException theme(Long id) {
        return new NotFoundException(NOT_FOUND_FORMAT.formatted(id, "테마"));
    }

    public static NotFoundException payment(String orderId) {
        return new NotFoundException("주문 %s에 대한 결제 정보를 찾을 수 없습니다.".formatted(orderId));
    }
}
