package roomescape.reservation.application.service;

import roomescape.reservation.application.dto.AvailableReservationTimeServiceRequest;
import roomescape.reservation.application.dto.AvailableReservationTimeServiceResponse;
import roomescape.reservation.application.dto.ThemeToBookCountServiceResponse;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationDate;
import roomescape.reservation.ui.ReservationSearchRequest;
import roomescape.reservation.ui.dto.ReservationResponse;
import roomescape.theme.domain.ThemeId;
import roomescape.time.domain.ReservationTimeId;
import roomescape.user.domain.UserId;

import java.util.List;

public interface ReservationQueryService {

    List<Reservation> getAll();

    List<AvailableReservationTimeServiceResponse> getTimesWithAvailability(AvailableReservationTimeServiceRequest availableReservationTimeServiceRequest);

    List<ThemeToBookCountServiceResponse> getRanking(ReservationDate startDate, ReservationDate endDate, int count);

    List<Reservation> getByParams(ReservationSearchRequest request);

    List<Reservation> getAllByUserId(UserId userId);

    boolean existsByTimeId(ReservationTimeId timeId);

    boolean existsByParams(ReservationDate date, ReservationTimeId timeId, ThemeId themeId);
}
