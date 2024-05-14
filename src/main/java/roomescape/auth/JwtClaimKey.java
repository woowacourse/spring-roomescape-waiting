package roomescape.auth;

public enum JwtClaimKey {
    MEMBER_ID("memberId"),
    MEMBER_NAME("memberName"),
    MEMBER_EMAIL("email"),
    ROLE("role"),
    ;

    private final String key;

    JwtClaimKey(final String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}
