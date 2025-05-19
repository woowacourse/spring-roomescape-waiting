package roomescape.common.domain;

public enum DomainTerm {

    // Reservation 도메인
    RESERVATION("예약"),
    RESERVATION_ID("예약 식별자"),
    RESERVATION_DATE("예약 날짜"),
    RESERVER_NAME("예약자 이름"),
    BOOKED_COUNT("예약 수"),
    BOOKED_STATUS("예약 상태"),

    // Theme 도메인
    THEME("테마"),
    THEME_ID("테마 식별자"),
    THEME_NAME("테마 이름"),
    THEME_DESCRIPTION("테마 설명"),
    THEME_THUMBNAIL("테마 썸네일"),

    // Time 도메인
    RESERVATION_TIME("예약 시간"),
    RESERVATION_TIME_ID("예약 시간 식별자"),

    // User 도메인
    USER("유저"),
    USER_ID("유저 식별자"),
    USER_NAME("유저 이름"),
    USER_EMAIL("유저 이메일"),
    USER_PASSWORD("유저 비밀번호"),
    USER_ROLE("유저 역할"),

    // Common
    DOMAIN_ID("도메인 식별자"),
    EMAIL("이메일");

    private final String label;

    DomainTerm(final String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }
}
