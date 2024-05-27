package roomescape.reservation.domain;

public enum ReservationStatus {
    RESERVED("예약"),
    WAITING("예약대기"),
    ;

    private String printName;

    ReservationStatus(String printName) {
        this.printName = printName;
    }

    public String getPrintName() {
        return printName;
    }
}
