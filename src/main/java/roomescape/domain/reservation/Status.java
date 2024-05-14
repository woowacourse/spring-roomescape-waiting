package roomescape.domain.reservation;

public enum Status {

    WAITING("예약 대기"),
    RESERVED("예약");

    private final String name;

    Status(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
