package roomescape.member.domain;

import jakarta.persistence.Column;
import java.util.Objects;
import roomescape.global.exception.member.InvalidMemberNameException;

public class MemberName {

    private static final int MIN_LENGTH = 1;
    private static final int MAX_LENGTH = 5;

    @Column(name = "name", nullable = false)
    private String value;

    public MemberName(String value) {
        validate(value);
        this.value = value;
    }

    protected MemberName() {
    }

    private void validate(String value) {
        validateNotNull(value);
        validateNotBlank(value);
        validateLength(value);
    }

    private void validateNotNull(String value) {
        if (value == null) {
            throw new InvalidMemberNameException("멤버 이름은 비어있을 수 없습니다");
        }
    }

    private void validateNotBlank(String value) {
        if (value.isBlank()) {
            throw new InvalidMemberNameException("멤버 이름은 공백문자로만 이루어질 수 없습니다");
        }
    }

    private void validateLength(String value) {
        int nameLength = value.length();
        if (nameLength < MIN_LENGTH || nameLength > MAX_LENGTH) {
            throw new InvalidMemberNameException("이름의 길이는 " + MIN_LENGTH + "자 이상, " + MAX_LENGTH + "자 이하여야 합니다");
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
        MemberName memberName = (MemberName) o;
        return Objects.equals(value, memberName.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
