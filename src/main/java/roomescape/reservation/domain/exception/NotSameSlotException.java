package roomescape.reservation.domain.exception;

public class NotSameSlotException extends RuntimeException {
    public NotSameSlotException() {
        super("같은 슬롯의 예약이 아닙니다.");
    }
}
