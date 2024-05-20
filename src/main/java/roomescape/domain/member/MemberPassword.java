package roomescape.domain.member;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.util.Objects;
import roomescape.exception.InvalidRequestException;

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
            throw new InvalidRequestException("비밀번호를 입력해주세요.");
        }
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MemberPassword that = (MemberPassword) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }
}
