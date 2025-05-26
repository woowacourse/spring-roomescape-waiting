package roomescape.reservation.application.service;

import roomescape.reservation.domain.WaitingReservation;

import java.util.List;

public interface WaitingReservationQueryService {

    List<WaitingReservation> getAll();

    Long findUserIdById(Long id);
}
