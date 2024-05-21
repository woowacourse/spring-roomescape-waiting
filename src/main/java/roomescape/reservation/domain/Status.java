package roomescape.reservation.domain;

public enum Status {

    SUCCESS("예약"),
    CANCEL("취소"),
    WAITING("대기"),
    ;

    final String message;

    Status(String message) {
        this.message = message;
    }

    public String getStatus() {
        return message;
    }
}
