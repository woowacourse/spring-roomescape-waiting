package roomescape.exception.custom;

public class AlreadyReservedException extends CustomException {

    public AlreadyReservedException() {
        super("이미 해당 슬롯에 예약을 신청했습니다.");
    }
}
