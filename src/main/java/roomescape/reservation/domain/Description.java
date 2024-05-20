package roomescape.reservation.domain;

import jakarta.persistence.Embeddable;
import java.util.regex.Pattern;

@Embeddable
public class Description {

    private static final Pattern DESCRIPTION_FORMAT = Pattern.compile("^[가-힣0-9\\s.,]+$");
    private static final int NAME_LENGTH = 50;

    private String text;

    public Description() {
    }

    public Description(final String text) {
        validate(text);
        this.text = text;
    }

    private void validate(String name) {
        validateFormat(name);
        validateLength(name);
    }

    private void validateFormat(String name) {
        if (!DESCRIPTION_FORMAT.matcher(name).matches()) {
            throw new IllegalArgumentException("설명에 영어 또는 특수문자를 포함할 수 없습니다.");
        }
    }

    private void validateLength(String name) {
        if (name.length() > NAME_LENGTH) {
            throw new IllegalArgumentException(String.format("%d자 까지만 입력가능합니다.", NAME_LENGTH));
        }
    }

    public String getText() {
        return text;
    }
}
