package roomescape.security.authentication;

public class AuthenticationHolder {

    private static final ThreadLocal<Authentication> authenticationHolder = new ThreadLocal<>();

    public static void clear() {
        authenticationHolder.remove();
    }

    public static void setAuthentication(Authentication authentication) {
        authenticationHolder.set(authentication);
    }

    public static Authentication getAuthentication() {
        return authenticationHolder.get();
    }
}
