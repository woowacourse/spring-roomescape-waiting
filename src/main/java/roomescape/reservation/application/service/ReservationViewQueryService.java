package roomescape.reservation.application.service;

import roomescape.reservation.application.dto.CreateReservationServiceRequest;
import roomescape.reservation.domain.ReservationView;

import java.util.List;

public interface ReservationViewQueryService {

    boolean existsByParams(CreateReservationServiceRequest serviceRequest, Long id);

    List<ReservationView> getAllByUserId(Long userId);
}
