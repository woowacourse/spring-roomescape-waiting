package roomescape.domain;

import jakarta.persistence.Embeddable;
import roomescape.exception.member.InvalidMemberNameLengthException;

@Embeddable
public class MemberName {
    private final static int MIN_LENGTH = 2;
    private final static int MAX_LENGTH = 5;
    private String name;

    protected MemberName() {
    }

    public MemberName(String name) {
        validate(name);
        this.name = name;
    }

    private void validate(String name) {
        if (name.length() < MIN_LENGTH || name.length() > MAX_LENGTH) {
            throw new InvalidMemberNameLengthException();
        }
    }

    public String getName() {
        return name;
    }
}
