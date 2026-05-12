package roomescape.exception;

public class ReservationNotFoundException extends RuntimeException {
    public ReservationNotFoundException(Long id) {
        super(id + "번 예약을 찾을 수 없습니다.");
    }
}
