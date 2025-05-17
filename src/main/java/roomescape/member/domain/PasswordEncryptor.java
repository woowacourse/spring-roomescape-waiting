package roomescape.member.domain;

public interface PasswordEncryptor {

    String encrypt(String password);
}
