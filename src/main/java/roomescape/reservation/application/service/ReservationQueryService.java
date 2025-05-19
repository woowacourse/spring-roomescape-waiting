package roomescape.reservation.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.reservation.application.dto.AvailableReservationTimeServiceRequest;
import roomescape.reservation.application.dto.AvailableReservationTimeServiceResponse;
import roomescape.reservation.application.dto.ThemeToBookCountServiceResponse;
import roomescape.reservation.domain.BookedCount;
import roomescape.reservation.domain.BookedStatus;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationDate;
import roomescape.reservation.domain.ReservationRepository;
import roomescape.reservation.ui.ReservationSearchRequest;
import roomescape.theme.domain.ThemeId;
import roomescape.time.application.service.ReservationTimeQueryService;
import roomescape.time.domain.ReservationTime;
import roomescape.time.domain.ReservationTimeId;
import roomescape.user.domain.UserId;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReservationQueryService {

    private final ReservationRepository reservationRepository;
    private final ReservationTimeQueryService reservationTimeQueryService;

    public List<Reservation> getAll() {
        return reservationRepository.findAll();
    }

    public List<AvailableReservationTimeServiceResponse> getTimesWithBookedStatus(
            final AvailableReservationTimeServiceRequest request) {
        final List<ReservationTime> allTimes = reservationTimeQueryService.getAll();
        final Set<ReservationTimeId> bookedTimeIds = new HashSet<>(reservationRepository.findTimeIdByParams(
                request.date(),
                request.themeId())
        );
        return allTimes.stream()
                .map(time -> createAvailabilityResponse(time, bookedTimeIds))
                .toList();
    }

    public List<ThemeToBookCountServiceResponse> getRanking(final ReservationDate startDate,
                                                            final ReservationDate endDate,
                                                            final int bookCount) {

        return reservationRepository.findThemesToBookedCountByParamsOrderByBookedCount(startDate, endDate, bookCount)
                .entrySet().stream()
                .map(entry -> new ThemeToBookCountServiceResponse(entry.getKey(), BookedCount.from(entry.getValue())))
                .toList();
    }

    public List<Reservation> getByParams(final ReservationSearchRequest request) {
        return reservationRepository.findAllByParams(
                request.userId(),
                request.themeId(),
                request.dateFrom(),
                request.dateTo()
        );
    }

    public List<Reservation> getAllByUserId(final UserId userId) {
        return reservationRepository.findAllByUserId(userId);
    }

    public boolean existsByTimeId(final ReservationTimeId timeId) {
        return reservationRepository.existsByParams(timeId);
    }

    public boolean existsByParams(final ReservationDate date,
                                  final ReservationTimeId timeId,
                                  final ThemeId themeId) {
        return reservationRepository.existsByParams(date, timeId, themeId);
    }

    private AvailableReservationTimeServiceResponse createAvailabilityResponse(final ReservationTime time,
                                                                               final Set<ReservationTimeId> bookedTimeIds) {
        final int bookingCount = (int) bookedTimeIds.stream()
                .filter(id -> id.equals(time.getId()))
                .count();

        final BookedStatus bookedStatus = BookedStatus.from(bookingCount);
        return new AvailableReservationTimeServiceResponse(time, bookedStatus);
    }
}
