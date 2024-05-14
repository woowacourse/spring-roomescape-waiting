package roomescape.reservation.domain;

import jakarta.persistence.Embeddable;
import java.util.regex.Pattern;

@Embeddable
public class ThemeName {

    private static final Pattern THEME_NAME_FORMAT = Pattern.compile("^[가-힣0-9a-zA-Z\\s]+$");
    private static final int NAME_LENGTH = 10;

    private String name;

    public ThemeName() {
    }

    public ThemeName(String name) {
        validate(name);
        this.name = name;
    }

    private void validate(String name) {
        validateFormat(name);
        validateLength(name);
    }

    private void validateFormat(String name) {
        if (!THEME_NAME_FORMAT.matcher(name).matches()) {
            throw new IllegalArgumentException("테마명은 공백, 한글, 영어, 숫자만 입력 가능합니다.");
        }
    }

    private void validateLength(String name) {
        if (name.length() > NAME_LENGTH) {
            throw new IllegalArgumentException(String.format("%d자 까지만 입력가능합니다.", NAME_LENGTH));
        }
    }

    public String getName() {
        return name;
    }
}
