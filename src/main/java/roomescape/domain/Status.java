package roomescape.domain;

public enum Status {

    CONFIRMED("예약"),
    WAITING("예약대기");

    private final String name;

    Status(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
