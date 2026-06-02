package roomescape.auth.session;

public final class SessionUtils {
    public static final String LOGIN_MEMBER_ATTRIBUTE = "loginMember";

    private SessionUtils() {
    }

    public static Long parseMemberId(Object raw) {
        if (raw instanceof Long l) return l;
        if (raw instanceof String s) return Long.parseLong(s);
        return null;
    }
}
