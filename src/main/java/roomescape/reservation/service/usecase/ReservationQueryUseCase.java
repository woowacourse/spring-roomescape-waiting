package roomescape.reservation.service.usecase;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.common.exception.NotFoundException;
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

    public List<Reservation> getByMemberId(final Long memberId) {
        return reservationRepository.findAllByInfoMemberId(memberId);
    }

    public List<AvailableReservationTimeServiceResponse> getTimesWithAvailability(
            final AvailableReservationTimeServiceRequest availableReservationTimeServiceRequest) {
        final List<ReservationTime> allTimes = reservationTimeQueryUseCase.getAll();

        final Set<Long> bookedTimeIds = new HashSet<>(reservationRepository.findByInfoDateAndInfoThemeId(
                        ReservationDate.from(availableReservationTimeServiceRequest.date()),
                        availableReservationTimeServiceRequest.themeId()).stream()
                .map(reservation -> reservation.getTime().getId())
                .toList()
        );

        final List<AvailableReservationTimeServiceResponse> responses = new ArrayList<>();

        for (final ReservationTime reservationTime : allTimes) {
            final boolean isBooked = bookedTimeIds.contains(reservationTime.getId());
            responses.add(new AvailableReservationTimeServiceResponse(
                    reservationTime.getStartAt(),
                    reservationTime.getId(),
                    isBooked));
        }

        return responses;
    }

    public List<ThemeToBookCountServiceResponse> getRanking(final ReservationDate startDate,
                                                            final ReservationDate endDate,
                                                            final int bookCount) {

        return reservationRepository.findThemesWithReservationCount(startDate, endDate, bookCount).stream()
                .map(ThemeToBookCountServiceResponse::new)
                .toList();
    }

    public Reservation getByParams(final ReservationDate date,
                                   final Long timeId,
                                   final Long themeId) {
        return reservationRepository.findByInfoDateAndInfoTimeIdAndInfoThemeId(date, timeId, themeId)
                .orElseThrow(NotFoundException::new);
    }

    public boolean existsByTimeId(final Long timeId) {
        return reservationRepository.existsByInfoTimeId(timeId);
    }

    public boolean existsByParams(final ReservationDate date,
                                  final Long timeId,
                                  final Long themeId) {
        return reservationRepository.existsByInfoDateAndInfoTimeIdAndInfoThemeId(date, timeId, themeId);
    }

    public List<Reservation> search(final Long memberId,
                                    final Long themeId,
                                    final ReservationDate from,
                                    final ReservationDate to) {
        return reservationRepository.findByInfoMemberIdAndInfoThemeIdAndInfoDateBetween(memberId, themeId, from, to);
    }
}
