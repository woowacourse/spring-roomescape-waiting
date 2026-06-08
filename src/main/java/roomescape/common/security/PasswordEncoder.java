package roomescape.common.security;

public interface PasswordEncoder {

    String encode(String rawPassword);

    boolean matches(String rawPassword, String encodedPassword);
}
