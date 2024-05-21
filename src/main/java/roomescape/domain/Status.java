package roomescape.domain;

public enum Status {

    RESERVED,
    WAITING,
    ;

    public boolean isReserved() {
        return this == RESERVED;
    }

    public boolean isWaiting() {
        return this == WAITING;
    }
}
