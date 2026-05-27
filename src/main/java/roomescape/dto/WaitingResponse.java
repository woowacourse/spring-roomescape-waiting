package roomescape.dto;

import roomescape.domain.Reservation;

public record WaitingResponse(long id, String name, int order) {
    public static WaitingResponse from(Reservation reservation, int order) {
        return new WaitingResponse(reservation.getId(), reservation.getName(), order);
    }
}
