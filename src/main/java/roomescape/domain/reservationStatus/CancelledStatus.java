package roomescape.domain.reservationStatus;

import roomescape.domain.Reservation;
import roomescape.global.exception.CustomException;
import roomescape.global.exception.ErrorCode;

public class CancelledStatus implements ReservationStatus {

    private CancelledStatus() {}

    private static class Holder {
        private static final CancelledStatus INSTANCE = new CancelledStatus();
    }

    public static CancelledStatus getInstance() {
        return Holder.INSTANCE;
    }

    @Override
    public void complete(Reservation reservation) {
        throw new CustomException(ErrorCode.ALREADY_CANCELLED_COMMAND);
    }

    @Override
    public void confirm(Reservation reservation) {
        throw new CustomException(ErrorCode.ALREADY_CANCELLED_COMMAND);
    }

    @Override
    public void cancel(Reservation reservation) {
        throw new CustomException(ErrorCode.INVALID_CANCELLED_COMMAND);
    }

    @Override
    public String getName() {
        return "CANCELLED";
    }
}
