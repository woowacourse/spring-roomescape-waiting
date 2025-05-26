package roomescape.reservation.service.usecase;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.common.exception.NotFoundException;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationDate;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.service.dto.AvailableReservationTimeServiceRequest;
import roomescape.reservation.service.dto.AvailableReservationTimeServiceResponse;
import roomescape.time.domain.ReservationTime;
import roomescape.time.service.usecase.ReservationTimeQueryUseCase;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReservationQueryUseCase {

    private final ReservationRepository reservationRepository;
    private final ReservationTimeQueryUseCase reservationTimeQueryUseCase;

    public List<Reservation> getAll() {
        return reservationRepository.findAll();
    }

    public Reservation get(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("예약을 찾을 수 없습니다."));
    }

    public List<Reservation> getByMemberId(final Long memberId) {
        return reservationRepository.findAllByMemberId(memberId);
    }

    public List<AvailableReservationTimeServiceResponse> getTimesWithAvailability(
            final AvailableReservationTimeServiceRequest availableReservationTimeServiceRequest) {
        final List<ReservationTime> allTimes = reservationTimeQueryUseCase.getAll();

        final Set<Long> bookedTimeIds = new HashSet<>(reservationRepository.findByParams(
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

    public boolean existsByTimeId(final Long timeId) {
        return reservationRepository.existsByTimeId(timeId);
    }

    public boolean existsByParams(final ReservationDate date,
                                  final Long timeId,
                                  final Long themeId) {
        return reservationRepository.existsByParams(date, timeId, themeId);
    }

    public boolean existsByParams(final ReservationDate date,
                                  final Long timeId,
                                  final Long themeId,
                                  final Long memberId) {
        return reservationRepository.existsByParams(date, timeId, themeId, memberId);
    }

    public List<Reservation> search(final Long memberId,
                                    final Long themeId,
                                    final ReservationDate from,
                                    final ReservationDate to) {
        return reservationRepository.findByParams(memberId, themeId, from, to);
    }
}
