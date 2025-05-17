package roomescape.auth.sign.password;

public interface PasswordEncoder {

    Password execute(String rawPassword);
}
