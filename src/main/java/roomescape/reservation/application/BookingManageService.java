package roomescape.reservation.application;

import org.springframework.stereotype.Service;
import roomescape.global.exception.ViolationException;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationRepository;
import roomescape.reservation.domain.ReservationStatus;

import java.util.Optional;

@Service
public class BookingManageService extends ReservationManageService {
    public BookingManageService(ReservationRepository reservationRepository) {
        super(reservationRepository);
    }

    @Override
    protected void scheduleForCreating(boolean existInSameTime, Reservation reservation) {
        if (existInSameTime && reservation.isBooking()) {
            reservation.changeToWaiting();
        }
    }

    @Override
    protected void scheduleForDeleting(Reservation deletedReservation) {
        Optional<Reservation> firstWaitingReservation = reservationRepository.findFirstByDateAndTimeAndThemeAndStatus(
                deletedReservation.getDate(), deletedReservation.getTime(),
                deletedReservation.getTheme(), ReservationStatus.WAITING);
        firstWaitingReservation.ifPresent(Reservation::changeToBooking);
    }

    @Override
    protected void validateReservationStatus(Reservation reservation) {
        if (!reservation.isBooking()) {
            throw new ViolationException("예약 상태가 예약 중이 아닙니다.");
        }
    }

    @Override
    protected void validatePermissionForDeleting(Reservation reservation, Member agent) {
        if (!agent.isAdmin()) {
            throw new ViolationException("예약을 삭제할 권한이 없습니다. 관리자만 삭제할 수 있습니다.");
        }
    }
}
