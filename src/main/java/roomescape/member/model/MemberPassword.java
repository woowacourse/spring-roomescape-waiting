package roomescape.member.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class MemberPassword {

    @Column(length = 1000, nullable = false)
    private String password;

    protected MemberPassword() {
    }

    public MemberPassword(final String password) {
        validateValue(password);
        this.password = password;
    }

    private void validateValue(final String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("회원 비밀번호로 공백을 입력할 수 없습니다.");
        }
    }

    public String getPassword() {
        return password;
    }
}
