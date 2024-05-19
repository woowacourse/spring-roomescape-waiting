package roomescape.service.dto;

import roomescape.domain.reservation.Reservation;

public record FindReservationWithRankDto(Reservation reservation, Long rank) { }
