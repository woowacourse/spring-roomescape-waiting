package roomescape.domain.member;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.util.regex.Pattern;

@Embeddable
public class MemberName {

    private static final int MAX_LENGTH = 30;
    private static final Pattern NAME_PATTERN = Pattern.compile("^[a-zA-Z가-힣]*$");

    @Column(nullable = false, length = MAX_LENGTH)
    private String name;

    protected MemberName() {
    }

    protected MemberName(String name) {
        validateBlank(name);
        validateLength(name);
        validatePattern(name);
        this.name = name;
    }

    private void validateBlank(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("이름은 필수 값입니다.");
        }
    }

    private void validateLength(String name) {
        if (MAX_LENGTH < name.length()) {
            throw new IllegalArgumentException(String.format("이름은 %d자를 넘을 수 없습니다.", MAX_LENGTH));
        }
    }

    private void validatePattern(String name) {
        if (!NAME_PATTERN.matcher(name).matches()) {
            throw new IllegalArgumentException("사용자명은 영어, 한글만 가능합니다.");
        }
    }

    protected String getValue() {
        return name;
    }
}
