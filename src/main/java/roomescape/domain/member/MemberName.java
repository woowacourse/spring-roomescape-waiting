package roomescape.domain.member;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class MemberName {

    @Column(name = "name")
    private String value;

    public MemberName() {
    }

    public MemberName(String value) {
        validateNullOrBlank(value);
        this.value = value;
    }

    private void validateNullOrBlank(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("이름을 입력해주세요.");
        }
    }

    public String getValue() {
        return value;
    }
}
