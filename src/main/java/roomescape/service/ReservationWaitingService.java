package roomescape.service;

import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import roomescape.domain.Reservation;
import roomescape.domain.Waiting;
import roomescape.infrastructure.ReservationRepository;
import roomescape.infrastructure.WaitingRepository;

@Service
public class ReservationWaitingService {

    private final ReservationRepository reservationRepository;
    private final WaitingRepository waitingRepository;

    public ReservationWaitingService(ReservationRepository reservationRepository, WaitingRepository waitingRepository) {
        this.reservationRepository = reservationRepository;
        this.waitingRepository = waitingRepository;
    }

    public void deleteIfNoWaitingOrUpdateReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException(String.format("예약 삭제 실패: 존재하지 않는 예약입니다. (id: %d)", reservationId)));
        LoggerFactory.getLogger(ReservationWaitingService.class).info("Hello");
        waitingRepository.findByDateAndTimeIdAndThemeIdOrderById(reservation.getDate(), reservation.getTime().getId(), reservation.getTheme().getId())
                .ifPresentOrElse(
                        waiting -> convertWaitingToReservation(waiting, reservation),
                        () -> reservationRepository.deleteById(reservationId));
    }

    private void convertWaitingToReservation(Waiting waiting, Reservation reservation) {
        Reservation changedReservation = new Reservation(reservation.getId(),
                waiting.getMember(),
                reservation.getDate(),
                reservation.getTime(),
                reservation.getTheme());
        reservationRepository.save(changedReservation);
        waitingRepository.deleteById(waiting.getId());
    }
}
