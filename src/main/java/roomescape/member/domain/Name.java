package roomescape.member.domain;

import roomescape.exceptions.MissingRequiredFieldException;

public record Name(String name) {

    public Name {
        validate(name);
    }

    private void validate(String name) {
        if (name == null || name.isBlank() || name.isEmpty()) {
            throw new MissingRequiredFieldException("사용자 이름은 필수 값입니다.");
        }
    }
}
