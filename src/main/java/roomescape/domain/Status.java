package roomescape.domain;

public enum Status {

    RESERVED("예약"),
    WAITING("예약대기");

    private final String value;

    Status(final String value) {
        this.value = value;
    }

    public static String getStatusValue(final long count) {
        if (count == 0) {
            return RESERVED.value;
        }

        return String.format("%s번째 %s", count, WAITING.value);
    }

    public String getValue() {
        return value;
    }
}
