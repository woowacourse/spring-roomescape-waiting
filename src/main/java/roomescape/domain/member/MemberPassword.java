package roomescape.domain.member;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class MemberPassword {

    @Column(name = "password")
    private String value;

    public MemberPassword() {
    }

    public MemberPassword(String value) {
        validateNullOrBlank(value);
        this.value = value;
    }

    public static void validateNullOrBlank(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("비밀번호를 입력해주세요.");
        }
    }

    public String getValue() {
        return value;
    }
}
