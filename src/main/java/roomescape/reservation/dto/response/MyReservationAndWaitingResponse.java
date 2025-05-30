package roomescape.reservation.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.waiting.domain.Waiting;
import roomescape.waiting.domain.WaitingWithRank;

import java.time.LocalDate;
import java.time.LocalTime;

public class MyReservationAndWaitingResponse {

    private final Long id;
    private final String themeName;
    private final LocalDate date;
    private final LocalTime time;
    private final String status;

    private MyReservationAndWaitingResponse(Long id, LocalDate date, LocalTime time, String themeName, String status) {
        this.id = id;
        this.date = date;
        this.time = time;
        this.themeName = themeName;
        this.status = status;
    }

    public static MyReservationAndWaitingResponse fromReservationAndStatus(Reservation reservation) {
        ReservationStatus status = reservation.getStatus();
        return new MyReservationAndWaitingResponse(
                reservation.getId(),
                reservation.getSchedule().getDate(),
                reservation.getSchedule().getTime().getStartAt(),
                reservation.getSchedule().getTheme().getName(),
                status.getDescription()
        );
    }

    public static MyReservationAndWaitingResponse fromWaitingAndStatus(WaitingWithRank waitingWithRank) {
        Waiting waiting = waitingWithRank.getWaiting();
        ReservationStatus status = waitingWithRank.getStatus();
        String statusWithRank = String.format("%s번째 %s", waitingWithRank.getRank() + 1, status.getDescription());
        return new MyReservationAndWaitingResponse(
                waiting.getId(),
                waiting.getSchedule().getDate(),
                waiting.getSchedule().getTime().getStartAt(),
                waiting.getSchedule().getTheme().getName(),
                statusWithRank
        );
    }

    public Long getId() {
        return id;
    }

    @JsonProperty("theme")
    public String getThemeName() {
        return themeName;
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
