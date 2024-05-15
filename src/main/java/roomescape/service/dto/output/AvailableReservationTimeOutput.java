package roomescape.service.dto.output;

import roomescape.dao.dto.AvailableReservationTimeResultInterface;

import java.util.List;

public record AvailableReservationTimeOutput(long timeId, String startAt, boolean alreadyBooked) {

    public static AvailableReservationTimeOutput toOutput(final AvailableReservationTimeResultInterface availableReservationTimeResult) {

        return new AvailableReservationTimeOutput(availableReservationTimeResult.getTimeId(), availableReservationTimeResult.getStartAt(), availableReservationTimeResult.getIsBooked());
    }

    public static List<AvailableReservationTimeOutput> toOutputs(final List<AvailableReservationTimeResultInterface> availableReservationTimeResult) {
        return availableReservationTimeResult.stream()
                .map(AvailableReservationTimeOutput::toOutput)
                .toList();
    }
}
