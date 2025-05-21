package roomescape.member.domain;

import jakarta.persistence.Embeddable;

@Embeddable
public class MemberName {

    private static final int MAX_LENGTH = 5;

    private String name;

    public MemberName(final String name) {
        validateName(name);
        this.name = name;
    }

    public MemberName() {
    }

    private void validateName(final String name) {
        if (name == null || name.isBlank() || name.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("예약자명은 최소 1글자, 최대 5글자여야합니다.");
        }
    }

    public String getName() {
        return name;
    }
}
