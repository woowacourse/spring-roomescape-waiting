package roomescape.reservation.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.common.domain.DomainTerm;
import roomescape.common.exception.NotFoundException;
import roomescape.reservation.application.dto.ReservationSearchRequest;
import roomescape.reservation.application.dto.ThemeToBookCountResponse;
import roomescape.reservation.domain.BookedCount;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationDate;
import roomescape.reservation.domain.ReservationId;
import roomescape.reservation.domain.ReservationRepository;
import roomescape.theme.domain.ThemeId;
import roomescape.timeslot.domain.ReservationTime;
import roomescape.user.domain.UserId;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReservationQueryService {

    private final ReservationRepository reservationRepository;

    public Reservation getById(final ReservationId id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(DomainTerm.RESERVATION, id));
    }

    public List<Reservation> getAll() {
        return reservationRepository.findAll();
    }

    public List<ThemeToBookCountResponse> getRanking(final ReservationDate startDate,
                                                     final ReservationDate endDate,
                                                     final int bookCount) {

        return reservationRepository.findThemesToBookedCountByParamsOrderByBookedCount(startDate, endDate, bookCount)
                .entrySet().stream()
                .map(entry -> new ThemeToBookCountResponse(entry.getKey(), BookedCount.from(entry.getValue())))
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

    public boolean existsByParams(final ReservationDate date,
                                  final ReservationTime time,
                                  final ThemeId themeId) {
        return reservationRepository.existsByParams(date, time, themeId);
    }
}
