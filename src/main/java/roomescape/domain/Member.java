package roomescape.domain;

import java.util.Objects;

public record Member(String name) {

    public Member {
        Objects.requireNonNull(name, "회원 이름은 필수입니다.");
    }
}
