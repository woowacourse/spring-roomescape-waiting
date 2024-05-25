package roomescape.reservation.application;

import roomescape.member.domain.Member;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationRepository;

public class DummyReservationManageService extends ReservationManageService {
    public DummyReservationManageService(ReservationRepository reservationRepository) {
        super(reservationRepository);
    }

    @Override
    protected void scheduleForCreating(boolean existInSameTime, Reservation reservation) {
    }

    @Override
    protected void scheduleForDeleting(Reservation deletedReservation) {
    }

    @Override
    protected void validateReservationStatus(Reservation reservation) {
    }

    @Override
    protected void validateOwnerShipForDeleting(Reservation reservation, Member agent) {
    }
}
