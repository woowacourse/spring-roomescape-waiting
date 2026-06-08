package roomescape.domain.reservation.entity;

public enum ReservationEditableStatus {

    EDITABLE(""),
    EDIT_RECOMMENDED("현재 예약의 시간 또는 테마가 더 이상 제공되지 않습니다. 다른 예약 정보로 수정해주세요."),
    LOCKED("지난 예약은 수정하거나 취소할 수 없습니다."),
    WAITING("대기 중인 예약입니다."),
    CANCELED("취소된 예약입니다.");

    private final String message;

    ReservationEditableStatus(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
