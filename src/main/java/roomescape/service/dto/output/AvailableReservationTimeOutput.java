package roomescape.service.dto.output;

import roomescape.domain.reservation.ReservationTime;

import java.util.ArrayList;
import java.util.List;

public record AvailableReservationTimeOutput(long timeId, String startAt, boolean alreadyBooked) {

    public static List<AvailableReservationTimeOutput> toOutputs(final List<ReservationTime> reservationTimes, final List<ReservationTime> bookedTimes) {
        final List<AvailableReservationTimeOutput> outputs = new ArrayList<>();
        for (final ReservationTime reservationTime : reservationTimes) {
            if (bookedTimes.contains(reservationTime)) {
                outputs.add(new AvailableReservationTimeOutput(reservationTime.getId(), reservationTime.getStartAtAsString(), true));
                continue;
            }
            outputs.add(new AvailableReservationTimeOutput(reservationTime.getId(), reservationTime.getStartAtAsString(), false));
        }
        return outputs;
    }
}
