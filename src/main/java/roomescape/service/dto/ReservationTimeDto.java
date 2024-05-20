package roomescape.service.dto;

import java.time.LocalTime;
import roomescape.controller.request.ReservationTimeRequest;
import roomescape.model.ReservationTime;

public class ReservationTimeDto {

    private final Long id;
    private final LocalTime startAt;

    private ReservationTimeDto(Long id, LocalTime startAt) {
        this.id = id;
        this.startAt = startAt;
    }

    public ReservationTimeDto(LocalTime startAt) {
        this(null, startAt);
    }

    public static ReservationTimeDto from(ReservationTimeRequest request) {
        return new ReservationTimeDto(null, request.getStartAt());
    }

    public static ReservationTimeDto from(ReservationTime reservationTime) {
        return new ReservationTimeDto(reservationTime.getId(), reservationTime.getStartAt());
    }

    public Long getId() {
        return id;
    }

    public LocalTime getStartAt() {
        return startAt;
    }
}
