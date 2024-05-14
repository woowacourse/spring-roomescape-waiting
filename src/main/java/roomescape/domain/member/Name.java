package roomescape.domain.member;

import java.util.regex.Pattern;

public class Name {

    private final String name;

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
        if (!Pattern.matches("^[a-zA-Zㄱ-ㅎ가-힣]*$", name)) {
            throw new IllegalArgumentException(
                    "[ERROR] 이름은 영어 또는 한글만 가능합니다.",
                    new Throwable("member_name : " + name)
            );
        }
    }

    private void validateNameLength(String name) {
        if (name.length() < 2 || name.length() > 10) {
            throw new IllegalArgumentException(
                    "[ERROR] 이름은 2~10자만 가능합니다.",
                    new Throwable("member_name : " + name)

            );
        }
    }
}
