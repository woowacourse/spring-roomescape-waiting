package roomescape.controller.response;

import java.time.LocalDate;
import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import roomescape.exception.NotFoundException;
import roomescape.model.Reservation;
import roomescape.model.ReservationStatus;

public class MemberReservationResponse {

    private Long reservationId;
    private String theme;
    private LocalDate date;
    @JsonFormat(pattern = "HH:mm")
    private LocalTime time;
    private String status;

    public MemberReservationResponse(Reservation reservation) {
        this.reservationId = reservation.getId();
        this.theme = reservation.getTheme().getName();
        this.date = reservation.getDate();
        this.time = reservation.getTime().getStartAt();
        this.status = mapStatus(reservation.getStatus());
    }

    public Long getReservationId() {
        return reservationId;
    }

    public String getTheme() {
        return theme;
    }

    public LocalDate getDate() {
        return date;
    }

    public LocalTime getTime() {
        return time;
    }

    public String getStatus() {
        return status;
    }

    public String mapStatus(ReservationStatus status) {
        switch (status) {
            case ACCEPT -> {
                return "예약";
            }
            case PENDING -> {
                return "대기";
            }
            default -> throw new NotFoundException("%s 상태를 표현하는 로직이 존재하지 않습니다.".formatted(status.name()));
        }
    }
}
