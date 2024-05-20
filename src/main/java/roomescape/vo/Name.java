package roomescape.vo;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.util.regex.Pattern;
import roomescape.exception.model.RoomEscapeException;
import roomescape.exception.global.NameExceptionCode;

@Embeddable
public class Name {

    private static final Pattern ILLEGAL_NAME_REGEX = Pattern.compile(".*[^\\w\\s가-힣].*");
    private static final int MAX_NAME_LENGTH = 255;

    @Column(nullable = false)
    private String name;

    public Name(String name) {
        validate(name);
        this.name = name;
    }

    public Name() {
    }

    public String getName() {
        return name;
    }

    private void validate(String name) {
        if (name == null || name.isBlank()) {
            throw new RoomEscapeException(NameExceptionCode.NAME_IS_NULL_OR_BLANK_EXCEPTION);
        }
        if (ILLEGAL_NAME_REGEX.matcher(name)
                .matches()) {
            throw new RoomEscapeException(NameExceptionCode.ILLEGAL_NAME_FORM_EXCEPTION);
        }
        if (name.length() > MAX_NAME_LENGTH) {
            throw new RoomEscapeException(NameExceptionCode.NAME_LENGTH_IS_OVER_MAX_COUNT);
        }
    }

    @Override
    public String toString() {
        return "Name{" +
                "name='" + name + '\'' +
                '}';
    }
}
