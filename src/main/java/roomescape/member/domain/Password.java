package roomescape.member.domain;

public class Password {

    private final String password;

    public Password(String password) {
        validatePassword(password);
        this.password = password;
    }

    private void validatePassword(String password) {
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("[ERROR] 비밀번호는 공백이 될 수 없습니다.");
        }
    }

    public String getPassword() {
        return password;
    }
}
