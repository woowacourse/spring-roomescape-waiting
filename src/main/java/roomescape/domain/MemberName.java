package roomescape.domain;

import java.util.Objects;
import java.util.regex.Pattern;

public class MemberName {
    private static final int MAX_NAME_LENGTH = 10;
    private static final String REGEX = "^[가-힣a-zA-Z]+$";
    private static final Pattern PATTERN = Pattern.compile(REGEX);

    private final String value;

    public MemberName(String value) {
        validate(value);
        this.value = value;
    }

    private void validate(String name) {
        validateLength(name);
        validateBlank(name);
        validateFormat(name);
    }

    private void validateLength(String name) {
        if (name.isEmpty() || name.length() > MAX_NAME_LENGTH) {
            throw new IllegalArgumentException(String.format("예약자 이름은 %s 이하로 입력해야 합니다.", MAX_NAME_LENGTH));
        }
    }

    private void validateBlank(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("예약자 이름은 필수로 입력해야 합니다.");
        }
    }

    private void validateFormat(String name) {
        if (!PATTERN.matcher(name).matches()) {
            throw new IllegalArgumentException("예약자 이름은 한글 또는 영어만 입력가능합니다.");
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
        return Objects.hashCode(value);
    }
}
