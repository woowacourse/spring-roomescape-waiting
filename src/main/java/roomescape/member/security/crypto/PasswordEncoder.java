package roomescape.member.security.crypto;

public interface PasswordEncoder {

    String encodePassword(String rawPassword);

    boolean matches(String rawPassword, String encodedPassword);

}
