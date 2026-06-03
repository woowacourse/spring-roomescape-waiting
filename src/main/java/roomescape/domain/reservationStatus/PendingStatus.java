package roomescape.domain.reservationStatus;

import roomescape.domain.Reservation;
import roomescape.global.exception.CustomException;
import roomescape.global.exception.ErrorCode;

public class PendingStatus implements ReservationStatus {

    private PendingStatus() {}

    private static class Holder {
        private static final PendingStatus INSTANCE = new PendingStatus();
    }

    public static PendingStatus getInstance() {
        return Holder.INSTANCE;
    }

    @Override
    public void cancel(Reservation reservation) {
        reservation.changeStatus(CancelledStatus.getInstance());
    }

    @Override
    public void confirm(Reservation reservation) {
        reservation.changeStatus(ConfirmedStatus.getInstance());
    }

    @Override
    public void complete(Reservation reservation) {
        throw new CustomException(ErrorCode.INVALID_PENDING_COMMAND);
    }

    @Override
    public String getName() {
        return "PENDING";
    }
}
