package roomescape.domain;

public enum Status {
    WAITING,
    RESERVED,
    CANCELED;

    public static Status from(boolean isWaiting) {
        if (isWaiting) {
            return WAITING;
        }
        return RESERVED;
    }
}
