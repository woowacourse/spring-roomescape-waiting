package roomescape.service.reservation;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservationwait.ReservationWait;
import roomescape.domain.reservationwait.ReservationWaitStatus;
import roomescape.exception.InvalidRequestException;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationWaitRepository;

@Service
public class ReservationDeleteService {

    private final ReservationRepository reservationRepository;
    private final ReservationWaitRepository reservationWaitRepository;

    public ReservationDeleteService(ReservationRepository reservationRepository,
                                    ReservationWaitRepository reservationWaitRepository) {
        this.reservationRepository = reservationRepository;
        this.reservationWaitRepository = reservationWaitRepository;
    }

    @Transactional
    public void deleteReservation(long id) {
        Reservation reservation = getReservation(id);
        if (hasReservationWait(reservation)) {
            ReservationWait reservationWait = getReservationWait(reservation);
            confirmReservationWait(reservationWait);
        }
        reservationRepository.deleteById(id);
    }

    private Reservation getReservation(long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new InvalidRequestException("존재하지 않는 예약 아이디 입니다."));
    }

    private boolean hasReservationWait(Reservation reservation) {
        return reservationWaitRepository.existsByDateAndTimeIdAndThemeIdAndStatus(
                reservation.getDate(),
                reservation.getReservationTime().getId(),
                reservation.getTheme().getId(),
                ReservationWaitStatus.WAITING);
    }

    private ReservationWait getReservationWait(Reservation reservation) {
        return reservationWaitRepository.findFirstByDateAndThemeIdAndTimeIdAndStatusOrderById(
                        reservation.getDate(),
                        reservation.getTheme().getId(),
                        reservation.getReservationTime().getId(),
                        ReservationWaitStatus.WAITING)
                .orElseThrow(() -> new InvalidRequestException("예약 대기가 존재하지 않습니다."));
    }

    private void confirmReservationWait(ReservationWait reservationWait) {
        moveToReservation(reservationWait);
        reservationWait.confirm();
    }

    private void moveToReservation(ReservationWait reservationWait) {
        reservationRepository.save(
                new Reservation(
                        reservationWait.getMember(),
                        reservationWait.getDate(),
                        reservationWait.getTime(),
                        reservationWait.getTheme()));
    }
}
