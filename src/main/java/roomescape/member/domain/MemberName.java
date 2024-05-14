package roomescape.member.domain;

import jakarta.persistence.Embeddable;
import java.util.regex.Pattern;

@Embeddable
public class MemberName {

    private static final Pattern MEMBER_NAME_FORMAT = Pattern.compile("^[가-힣]+$");
    private static final int NAME_LENGTH = 4;

    private String name;

    public MemberName() {
    }

    public MemberName(String name) {
        validate(name);
        this.name = name;
    }

    private void validate(String name) {
        validateFormat(name);
        validateLength(name);
    }

    private void validateFormat(String name) {
        if (!MEMBER_NAME_FORMAT.matcher(name).matches()) {
            throw new IllegalArgumentException("이름은 한글로만 입력 가능합니다.");
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
