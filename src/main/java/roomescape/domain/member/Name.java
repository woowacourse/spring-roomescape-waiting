package roomescape.domain.member;

import jakarta.persistence.Embeddable;
import java.util.regex.Pattern;

@Embeddable
public class Name {

    private static final String NAME_PATTERN = "^[a-zA-Zㄱ-ㅎ가-힣]*$";
    private static final int MIN_NAME_LENGTH = 2;
    private static final int MAX_NAME_LENGTH = 10;

    private String name;

    protected Name() {
    }

    public Name(String name) {
        validateName(name);
        this.name = name;
    }

    public String getName() {
        return name;
    }

    private void validateName(String name) {
        validateNameFormat(name);
        validateNameLength(name);
    }

    private void validateNameFormat(String name) {
        if (!Pattern.matches(NAME_PATTERN, name)) {
            throw new IllegalArgumentException(
                    "[ERROR] 이름은 영어 또는 한글만 가능합니다.",
                    new Throwable("member_name : " + name)
            );
        }
    }

    private void validateNameLength(String name) {
        if (name.length() < MIN_NAME_LENGTH || name.length() > MAX_NAME_LENGTH) {
            throw new IllegalArgumentException(
                    "[ERROR] 이름은 2~10자만 가능합니다.",
                    new Throwable("member_name : " + name)
            );
        }
    }
}
