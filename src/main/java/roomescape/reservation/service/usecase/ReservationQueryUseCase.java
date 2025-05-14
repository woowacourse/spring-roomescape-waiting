package roomescape.reservation.service.usecase;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationDate;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.service.dto.AvailableReservationTimeServiceRequest;
import roomescape.reservation.service.dto.AvailableReservationTimeServiceResponse;
import roomescape.reservation.service.dto.ThemeToBookCountServiceResponse;
import roomescape.time.domain.ReservationTime;
import roomescape.time.service.usecase.ReservationTimeQueryUseCase;

@Service
@RequiredArgsConstructor
public class ReservationQueryUseCase {

    private final ReservationRepository reservationRepository;
    private final ReservationTimeQueryUseCase reservationTimeQueryUseCase;

    public List<Reservation> getAll() {
        return reservationRepository.findAll();
    }

    public List<Reservation> getAllByMemberId(final Long memberId) {
        return reservationRepository.findAllByMemberId(memberId);
    }

    public List<AvailableReservationTimeServiceResponse> getTimesWithAvailability(
            final AvailableReservationTimeServiceRequest availableReservationTimeServiceRequest) {
        final List<ReservationTime> allTimes = reservationTimeQueryUseCase.getAll();

        final Set<Long> bookedTimeIds = new HashSet<>(reservationRepository.findTimeIdByParams(
                ReservationDate.from(availableReservationTimeServiceRequest.date()),
                availableReservationTimeServiceRequest.themeId())
        );

        final List<AvailableReservationTimeServiceResponse> responses = new ArrayList<>();

        for (final ReservationTime reservationTime : allTimes) {
            final boolean isBooked = bookedTimeIds.contains(reservationTime.getId());
            responses.add(new AvailableReservationTimeServiceResponse(
                    reservationTime.getTime(),
                    reservationTime.getId(),
                    isBooked));
        }

        return responses;
    }

    public List<ThemeToBookCountServiceResponse> getRanking(final ReservationDate startDate,
                                                            final ReservationDate endDate,
                                                            final int bookCount) {

        return reservationRepository.findThemesToBookedCountByParamsOrderByBookedCount(startDate, endDate, bookCount)
                .entrySet().stream()
                .map(entry -> new ThemeToBookCountServiceResponse(entry.getKey(), entry.getValue()))
                .toList();
    }

    public boolean existsByTimeId(final Long timeId) {
        return reservationRepository.existsByParams(timeId);
    }

    public boolean existsByParams(final ReservationDate date,
                                  final Long timeId,
                                  final Long themeId) {
        return reservationRepository.existsByParams(date, timeId, themeId);
    }

    public List<Reservation> search(final Long memberId,
                                    final Long themeId,
                                    final ReservationDate from,
                                    final ReservationDate to) {
        return reservationRepository.findByParams(memberId, themeId, from, to);
    }
}
