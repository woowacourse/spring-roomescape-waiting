package roomescape.domain;

public enum Status {

    CREATED("예약"),
    WAITING("대기"),
    WAITING_CANCEL("대기 취소"),
    DELETED("삭제");

    private final String value;

    Status(String value) {
        this.value = value;
    }

    public Status delete() {
        return switch (this) {
            case CREATED -> DELETED;
            case WAITING -> WAITING_CANCEL;
            default -> this;
        };
    }
}
