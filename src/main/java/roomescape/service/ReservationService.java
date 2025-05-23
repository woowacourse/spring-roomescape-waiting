package roomescape.service;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.controller.dto.request.ReservationSearchCondition;
import roomescape.domain.Reservation;
import roomescape.domain.Waiting;
import roomescape.domain.repository.ReservationRepository;
import roomescape.domain.repository.WaitingRepository;
import roomescape.exception.NotFoundException;
import roomescape.service.dto.result.ReservationResult;

@Service
@Transactional(readOnly = true)
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final WaitingRepository waitingRepository;

    public ReservationService(ReservationRepository reservationRepository, WaitingRepository waitingRepository) {
        this.reservationRepository = reservationRepository;
        this.waitingRepository = waitingRepository;
    }

    public List<ReservationResult> getReservationsInConditions(ReservationSearchCondition condition) {
        List<Reservation> reservations = reservationRepository.findReservationsInConditions(
                condition.memberId(),
                condition.themeId(),
                condition.dateFrom(),
                condition.dateTo()
        );

        return reservations.stream()
                .map(ReservationResult::from)
                .toList();
    }

    @Transactional
    public void deleteByIdAndReserveNextWaiting(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new NotFoundException("reservationId", reservationId));

        reservationRepository.deleteById(reservationId);

        boolean reservationSlotEmpty = reservationRepository.isReservationSlotEmpty(
                reservation.getDate(), reservation.getTime().getId(), reservation.getTheme().getId());
        if(reservationSlotEmpty) {
            autoReserveNextWaiting(reservation);
        }
    }

    private void autoReserveNextWaiting(Reservation canceled) {
        Optional<Waiting> firstWaiting = waitingRepository.findFirstWaiting(
                canceled.getDate(), canceled.getTheme().getId(), canceled.getTime().getId());

        firstWaiting.ifPresent(waiting -> {
            Reservation reservation = waiting.confirm();
            reservationRepository.save(reservation);
            waitingRepository.delete(waiting);
        });
    }

}
