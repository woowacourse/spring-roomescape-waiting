package roomescape.reservation.application.service;

import roomescape.reservation.application.dto.AvailableReservationTimeServiceRequest;
import roomescape.reservation.application.dto.AvailableReservationTimeServiceResponse;
import roomescape.reservation.application.dto.ReservationSearchRequest;
import roomescape.reservation.application.dto.ThemeToBookCountServiceResponse;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationDate;

import java.util.List;

public interface ReservationQueryService {

    List<Reservation> getAll();

    List<AvailableReservationTimeServiceResponse> getTimesWithAvailability(
            AvailableReservationTimeServiceRequest availableReservationTimeServiceRequest);

    List<ThemeToBookCountServiceResponse> getRanking(ReservationDate startDate, ReservationDate endDate, int count);

    List<Reservation> getByParams(ReservationSearchRequest request);

    List<Reservation> getAllReservationsByUserId(Long userId);

    boolean existsByTimeId(Long timeId);

    boolean existsByParams(ReservationDate date, Long timeId, Long themeId);
}
