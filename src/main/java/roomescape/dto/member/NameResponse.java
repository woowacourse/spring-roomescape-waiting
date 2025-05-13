package roomescape.dto.member;

import roomescape.domain.ReservationName;

public record NameResponse(Long id, String name) {

    public static NameResponse from(ReservationName name) {
        return new NameResponse(name.getId(), name.getName());
    }
}
