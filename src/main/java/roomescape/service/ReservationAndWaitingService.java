package roomescape.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationDate;
import roomescape.domain.ReservationWaiting;
import roomescape.infrastructure.ReservationRepository;
import roomescape.infrastructure.ReservationWaitingRepository;

import java.util.Optional;

@Service
public class ReservationAndWaitingService {

    private final ReservationRepository reservationRepository;
    private final ReservationWaitingRepository reservationWaitingRepository;

    public ReservationAndWaitingService(ReservationRepository reservationRepository, ReservationWaitingRepository reservationWaitingRepository) {
        this.reservationRepository = reservationRepository;
        this.reservationWaitingRepository = reservationWaitingRepository;
    }

    @Transactional
    public void changeWaitingToReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException(String.format("예약 삭제 실패: 존재하지 않는 예약입니다. (id: %d)", reservationId)));
        findWaitingOfReservation(reservation).ifPresentOrElse(
                waiting -> updateReservationByWaiting(waiting, reservation),
                () -> deleteReservation(reservationId));
    }

    private Optional<ReservationWaiting> findWaitingOfReservation(Reservation reservation) {
        ReservationDate date = reservation.getDate();
        Long timeId = reservation.getTime().getId();
        Long themeId = reservation.getTheme().getId();
        return reservationWaitingRepository.findTopByDateAndTimeIdAndThemeIdOrderById(date, timeId, themeId);
    }

    private void updateReservationByWaiting(ReservationWaiting waiting, Reservation reservation) {
        Reservation changedReservation = new Reservation(reservation.getId(),
                waiting.getMember(),
                reservation.getDate(),
                reservation.getTime(),
                reservation.getTheme());
        reservationRepository.save(changedReservation);
        reservationWaitingRepository.deleteById(waiting.getId());
    }

    private void deleteReservation(Long reservationId) {
        reservationRepository.deleteById(reservationId);
    }
}
