package roomescape.controller.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import roomescape.model.Reservation;
import roomescape.model.Waiting;
import roomescape.model.WaitingWithRank;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Stream;

public class OwnReservationResponse {

    private final long id;
    private final String theme;
    private final LocalDate date;
    private final LocalTime time;
    private final String status;

    private OwnReservationResponse(long id, String theme, LocalDate date, LocalTime time, String status) {
        this.id = id;
        this.theme = theme;
        this.date = date;
        this.time = time;
        this.status = status;
    }

    private static OwnReservationResponse from(Reservation reservation) {
        return new OwnReservationResponse(
                reservation.getId(),
                reservation.getTheme().getName(),
                reservation.getDate(),
                reservation.getTime().getStartAt(),
                "예약");
    }

    private static OwnReservationResponse from(WaitingWithRank waitingWithRank) {
        Waiting waiting = waitingWithRank.getWaiting();
        Long rank = waitingWithRank.getRank();
        return new OwnReservationResponse(
                waiting.getId(),
                waiting.getTheme().getName(),
                waiting.getDate(),
                waiting.getTime().getStartAt(),
                rank + "번째 예약대기");
    }

    public static List<OwnReservationResponse> from(List<Reservation> reservations, List<WaitingWithRank> waitingWithRank) {
        List<OwnReservationResponse> reservationResponses = mapFromReservations(reservations);
        List<OwnReservationResponse> waitingResponses = mapFromWaiting(waitingWithRank);
        return Stream.concat(reservationResponses.stream(), waitingResponses.stream()).toList();
    }

    private static List<OwnReservationResponse> mapFromReservations(List<Reservation> reservations) {
        return reservations.stream()
                .map(OwnReservationResponse::from)
                .toList();
    }

    private static List<OwnReservationResponse> mapFromWaiting(List<WaitingWithRank> waitingWithRank) {
        return waitingWithRank.stream()
                .map(OwnReservationResponse::from)
                .toList();
    }

    public long getId() {
        return id;
    }

    public String getTheme() {
        return theme;
    }

    public LocalDate getDate() {
        return date;
    }

    @JsonFormat(pattern = "HH:mm")
    public LocalTime getTime() {
        return time;
    }

    public String getStatus() {
        return status;
    }
}
