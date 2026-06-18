package roomescape.domain.reservationStatus;

import roomescape.domain.Reservation;
import roomescape.global.exception.CustomException;
import roomescape.global.exception.ErrorCode;

public class ConfirmedStatus implements ReservationStatus {

    private ConfirmedStatus() {}

    private static class Holder {
        private static final ConfirmedStatus INSTANCE = new ConfirmedStatus();

    }

    public static ConfirmedStatus getInstance() {
        return Holder.INSTANCE;
    }

    @Override
    public void cancel(Reservation reservation) {
        reservation.changeStatus(CancelledStatus.getInstance());
    }

    @Override
    public void confirm(Reservation reservation) {
        throw new CustomException(ErrorCode.INVALID_CONFIRMED_COMMAND);
    }

    @Override
    public void complete(Reservation reservation) {
        reservation.changeStatus(CompletedStatus.getInstance());
    }

    @Override
    public String getName() {
        return "CONFIRMED";
    }
}
