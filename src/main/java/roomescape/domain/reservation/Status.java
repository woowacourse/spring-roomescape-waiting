package roomescape.domain.reservation;

public enum Status {
    APPROVED("승인"),
    WAITING("대기");

    private final String koreanName;

    Status(String koreanName) {
        this.koreanName = koreanName;
    }

    public String getKoreanName() {
        return koreanName;
    }

    public static Status from(String value) {
        try {
            return Status.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("알 수 없는 status 값: " + value, e);
        }
    }
}
