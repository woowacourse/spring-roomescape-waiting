package roomescape.member.domain;

import roomescape.reservation.domain.exception.ReserverNameEmptyException;

public class Name {

    private final String name;

    public Name(String name) {
        this.name = validateBlank(name);
    }

    private String validateBlank(String name) {
        if (name == null || name.isBlank()) {
            throw new ReserverNameEmptyException("[ERROR] 예약자 이름은 필수입니다.");
        }
        return name;
    }

    public String getName() {
        return name;
    }
}
