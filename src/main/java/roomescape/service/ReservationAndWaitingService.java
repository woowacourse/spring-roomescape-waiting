package roomescape.service;

import org.springframework.stereotype.Service;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationWaiting;
import roomescape.infrastructure.ReservationRepository;
import roomescape.infrastructure.ReservationWaitingRepository;

@Service
public class ReservationAndWaitingService {

    private final ReservationRepository reservationRepository;
    private final ReservationWaitingRepository reservationWaitingRepository;

    public ReservationAndWaitingService(ReservationRepository reservationRepository, ReservationWaitingRepository reservationWaitingRepository) {
        this.reservationRepository = reservationRepository;
        this.reservationWaitingRepository = reservationWaitingRepository;
    }

    public void deleteIfNoWaitingOrUpdateReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException(String.format("예약 삭제 실패: 존재하지 않는 예약입니다. (id: %d)", reservationId)));
        reservationWaitingRepository.findByDateAndTimeIdAndThemeIdOrderById(reservation.getDate(), reservation.getTime().getId(), reservation.getTheme().getId())
                .ifPresentOrElse(
                        waiting -> convertWaitingToReservation(waiting, reservation),
                        () -> reservationRepository.deleteById(reservationId));
    }

    private void convertWaitingToReservation(ReservationWaiting waiting, Reservation reservation) {
        Reservation changedReservation = new Reservation(reservation.getId(),
                waiting.getMember(),
                reservation.getDate(),
                reservation.getTime(),
                reservation.getTheme());
        reservationRepository.save(changedReservation);
        reservationWaitingRepository.deleteById(waiting.getId());
    }
}
