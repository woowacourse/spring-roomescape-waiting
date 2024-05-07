package roomescape.controller.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import roomescape.model.ReservationTime;
import roomescape.service.dto.ReservationTimeInfoDto;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Stream;

public class ReservationTimeInfoResponse {

    private final long timeId;
    private final LocalTime startAt;
    private final boolean isBooked;

    private ReservationTimeInfoResponse(long timeId, LocalTime startAt, boolean isBooked) {
        this.timeId = timeId;
        this.startAt = startAt;
        this.isBooked = isBooked;
    }

    public static List<ReservationTimeInfoResponse> from(ReservationTimeInfoDto timesInfo) {
        List<ReservationTimeInfoResponse> bookedTimesResponse = toResponse(timesInfo.getBookedTimes(), true);
        List<ReservationTimeInfoResponse> notBookedTimesResponse = toResponse(timesInfo.getNotBookedTimes(), false);
        return Stream
                .concat(bookedTimesResponse.stream(), notBookedTimesResponse.stream())
                .toList();
    }

    private static List<ReservationTimeInfoResponse> toResponse(List<ReservationTime> times, boolean isBooked) {
        return times.stream()
                .map(time -> ReservationTimeInfoResponse.toResponse(time, isBooked))
                .toList();
    }

    private static ReservationTimeInfoResponse toResponse(ReservationTime time, boolean isBooked) {
        return new ReservationTimeInfoResponse(
                time.getId(),
                time.getStartAt().truncatedTo(ChronoUnit.SECONDS),
                isBooked);
    }

    public long getTimeId() {
        return timeId;
    }

    @JsonFormat(pattern = "HH:mm")
    public LocalTime getStartAt() {
        return startAt;
    }

    public boolean getIsBooked() {
        return isBooked;
    }

    @Override
    public String toString() {
        return "MemberReservationTimeResponse{" +
                "timeId=" + timeId +
                ", startAt=" + startAt +
                ", isBooked=" + isBooked +
                '}';
    }
}
