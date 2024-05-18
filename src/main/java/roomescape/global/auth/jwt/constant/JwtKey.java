package roomescape.global.auth.jwt.constant;

public enum JwtKey {
    MEMBER_ID("member_id"),
    ;

    private final String value;

    JwtKey(final String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
