package roomescape.domain;

public interface PasswordEncoder {
    Password encode(String rawPassword);

    Password encode(String rawPassword, String salt);
}
