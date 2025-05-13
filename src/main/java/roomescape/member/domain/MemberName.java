package roomescape.member.domain;

public class MemberName {

    private static final int MAX_NAME_LENGTH = 10;

    private final String name;

    public MemberName(String name) {
        validate(name);
        this.name = name;
    }

    private void validate(String name) {
        if (name == null || name.isBlank() || name.length() > MAX_NAME_LENGTH) {
            throw new IllegalArgumentException("[ERROR] 이름은 1글자 이상 10글자 이하여야합니다.");
        }
    }

    public String getName() {
        return name;
    }
}
