package roomescape.domain.reservationStatus;

import roomescape.domain.Reservation;
import roomescape.global.exception.CustomException;
import roomescape.global.exception.ErrorCode;

public class CompletedStatus implements ReservationStatus {

    private CompletedStatus() {}

    private static class Holder {
        private static final CompletedStatus INSTANCE = new CompletedStatus();
    }

    public static CompletedStatus getInstance() {
        return Holder.INSTANCE;
    }

    @Override
    public void complete(Reservation reservation) {
        throw new CustomException(ErrorCode.INVALID_COMPLETED_COMMAND);
    }

    @Override
    public void confirm(Reservation reservation) {
        throw new CustomException(ErrorCode.INVALID_COMPLETED_COMMAND);
    }

    @Override
    public void cancel(Reservation reservation) {
        throw new CustomException(ErrorCode.INVALID_CANCELLED_COMMAND);
    }

    @Override
    public String getName() {
        return "COMPLETED";
    }
}
