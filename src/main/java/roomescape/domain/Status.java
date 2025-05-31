package roomescape.domain;

public enum Status {
    CONFIRMED("CONFIRMED"),
    PENDING("PENDING");

    private final String status;

    Status(final String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}
