package roomescape.controller.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import roomescape.model.Reservation;
import roomescape.model.Waiting;
import roomescape.model.WaitingWithRank;

import java.time.LocalDate;
import java.time.LocalTime;

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
        this.status = "예약";
    }

    public MemberReservationResponse(WaitingWithRank waitingWithRank) {
        this.reservationId = waitingWithRank.getWaiting().getId();
        this.theme = waitingWithRank.getWaiting().getTheme().getName();
        this.date = waitingWithRank.getWaiting().getDate();
        this.time = waitingWithRank.getWaiting().getTime().getStartAt();
        this.status = "%d번째 예약대기".formatted(waitingWithRank.getRank());
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
}
