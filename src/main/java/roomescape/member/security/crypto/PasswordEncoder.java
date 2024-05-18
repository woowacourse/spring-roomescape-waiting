package roomescape.member.security.crypto;

public interface PasswordEncoder {
    boolean matches(String rawPassword, String encodedPassword);
}
