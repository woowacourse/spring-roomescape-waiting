package roomescape.reservation.service;

import java.util.Optional;
import org.springframework.stereotype.Service;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.Status;
import roomescape.reservation.repository.ReservationRepository;

@Service
public class ReservationSchedulerService {

    private final ReservationRepository reservationRepository;

    public ReservationSchedulerService(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    public void validateSaveReservation(Reservation reservation) {
        validateMemberReservationUnique(reservation);
        validateReservationAvailable(reservation);
    }

    public void validateSaveWaiting(Reservation reservation) {
        validateMemberReservationUnique(reservation);
        validateWaitingAvailable(reservation);
    }

    public void validateApproveReservation(Reservation reservation) {
        if (findSuccessReservation(reservation).isPresent()) {
            throw new IllegalArgumentException("이미 확정된 예약이 있습니다.");
        }
    }

    private void validateMemberReservationUnique(Reservation reservation) {
        findMemberReservation(reservation).ifPresent(this::throwExceptionByStatus);
    }

    private void throwExceptionByStatus(Reservation memberReservation) {
        if (memberReservation.isWaitingReservation()) {
            throw new IllegalArgumentException("이미 회원이 예약 대기한 내역이 있습니다.");
        }
        if (memberReservation.isSuccessReservation()) {
            throw new IllegalArgumentException("이미 회원이 예약한 내역이 있습니다.");
        }
    }

    private void validateReservationAvailable(Reservation reservation) {
        if (findReservation(reservation).isPresent()) {
            throw new IllegalArgumentException("이미 예약이 다 찼습니다. 예약 대기를 걸어주세요.");
        }
    }

    private void validateWaitingAvailable(Reservation reservation) {
        if (findReservation(reservation).isEmpty()) {
            throw new IllegalArgumentException("추가된 예약이 없어 대기 등록을 할 수 없습니다.");
        }
    }

    private Optional<Reservation> findMemberReservation(Reservation reservation) {
        return reservationRepository.findFirstByDateAndReservationTimeAndThemeAndMember(
                reservation.getDate(),
                reservation.getTime(),
                reservation.getTheme(),
                reservation.getMember()
        );
    }

    private Optional<Reservation> findReservation(Reservation reservation) {
        return reservationRepository.findFirstByDateAndReservationTimeAndTheme(
                reservation.getDate(),
                reservation.getTime(),
                reservation.getTheme()
        );
    }

    private Optional<Reservation> findSuccessReservation(Reservation reservation) {
        return reservationRepository.findFirstByDateAndReservationTimeAndThemeAndStatus(
                reservation.getDate(),
                reservation.getTime(),
                reservation.getTheme(),
                Status.SUCCESS
        );
    }
}
