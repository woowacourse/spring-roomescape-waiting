package roomescape.reservation.application.service;

import roomescape.reservation.application.dto.CreateReservationServiceRequest;
import roomescape.reservation.domain.ReservationView;

import java.util.List;
import java.util.Optional;

public interface ReservationViewQueryService {

    boolean existsByParams(CreateReservationServiceRequest serviceRequest, Long id);

    List<ReservationView> getAllByUserId(Long userId);

    Optional<Long> findFirstWaitingByReservationId(Long id);
}
