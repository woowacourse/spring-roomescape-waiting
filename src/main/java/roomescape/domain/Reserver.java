package roomescape.domain;

import roomescape.domain.exception.RoomescapeException;

import java.util.Objects;

import static roomescape.domain.exception.DomainErrorCode.INVALID_INPUT;
import static roomescape.domain.exception.DomainErrorCode.UNAUTHORIZED_RESERVATION;
import static roomescape.domain.exception.DomainPreconditions.require;
import static roomescape.domain.exception.DomainPreconditions.requireNonBlank;

public class Reserver {

    private static final int MAX_NAME_SIZE = 255;

    private final String name;

    public Reserver(String name) {
        String validatedName = requireNonBlank(name, INVALID_INPUT, "예약자 이름은 비거나 공백일 수 없습니다.");
        require(validatedName.length() <= MAX_NAME_SIZE, INVALID_INPUT, String.format( "예약자 이름은 %d자를 초과할 수 없습니다.", MAX_NAME_SIZE));
        this.name = validatedName;
    }

    public String getName() {
        return name;
    }

    public void validateSameReserver(Reserver reserver) {
        if (!this.equals(reserver)) {
            throw new RoomescapeException(UNAUTHORIZED_RESERVATION, "본인의 예약만 변경할 수 있습니다.");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Reserver reserver = (Reserver) o;
        return Objects.equals(name, reserver.name);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }
}
