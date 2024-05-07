package roomescape.service.dto;

import roomescape.model.ReservationTime;

import java.util.List;

public class ReservationTimeInfoDto {

    private final List<ReservationTime> bookedTimes;
    private final List<ReservationTime> notBookedTimes;

    public ReservationTimeInfoDto(List<ReservationTime> bookedTimes, List<ReservationTime> notBookedTimes) {
        this.bookedTimes = bookedTimes;
        this.notBookedTimes = notBookedTimes;
    }

    public List<ReservationTime> getBookedTimes() {
        return bookedTimes;
    }

    public List<ReservationTime> getNotBookedTimes() {
        return notBookedTimes;
    }
}
