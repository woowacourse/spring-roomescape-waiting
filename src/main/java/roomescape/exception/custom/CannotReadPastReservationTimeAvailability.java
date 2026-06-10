package roomescape.exception.custom;

public class CannotReadPastReservationTimeAvailability extends CustomException {

    public CannotReadPastReservationTimeAvailability() {
        super("[ERROR] 지나간 날짜의 예약 가능 시간은 조회할 수 없습니다.");
    }
}
