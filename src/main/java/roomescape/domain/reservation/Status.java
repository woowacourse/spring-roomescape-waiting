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
}
