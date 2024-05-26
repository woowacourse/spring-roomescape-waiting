package roomescape.domain.member;

public class Password {

    private String password;

    protected Password() {
    }

    public Password(String password) {
        validatePassword(password);
        this.password = password;
    }

    private void validatePassword(String password) {
        if (password.length() < 4) {
            throw new IllegalArgumentException(
                    "[ERROR] 비밀번호는 4자 이상만 가능합니다.",
                    new Throwable("password_length : " + password.length()));
        }
    }

    public String getPassword() {
        return password;
    }
}
