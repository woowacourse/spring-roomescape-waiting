package roomescape.member.business.domain;

import java.util.regex.Pattern;

public class Email {

    private static final Pattern emailPattern = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");

    private final String value;

    public Email(final String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
