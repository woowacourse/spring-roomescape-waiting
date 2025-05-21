package roomescape.member.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class Password {

    @Column(nullable = false)
    private String password;

    public Password(String password) {
        validateBlank(password);
        this.password = password;
    }

    protected Password() {

    }

    private void validateBlank(String password) {
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("[ERROR] 비밀번호는 공백이 될 수 없습니다.");
        }
    }

    public String getPassword() {
        return password;
    }
}
