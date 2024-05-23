package roomescape.controller.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import roomescape.model.Reservation;
import roomescape.model.Waiting;
import roomescape.model.WaitingWithRank;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Stream;

public class MemberReservationResponse {

    private final long id;
    private final String theme;
    private final LocalDate date;
    private final LocalTime time;
    private final String status;

    private MemberReservationResponse(long id, String theme, LocalDate date, LocalTime time, String status) {
        this.id = id;
        this.theme = theme;
        this.date = date;
        this.time = time;
        this.status = status;
    }

    public static MemberReservationResponse from(Reservation reservation) {
        return new MemberReservationResponse(
                reservation.getId(),
                reservation.getTheme().getName(),
                reservation.getDate(),
                reservation.getTime().getStartAt(),
                "예약"); // TODO: 프론트에서?
    }

    private static MemberReservationResponse from(WaitingWithRank waitingWithRank) {
        Waiting waiting = waitingWithRank.getWaiting();
        Long rank = waitingWithRank.getRank();
        return new MemberReservationResponse(
                waiting.getId(),
                waiting.getTheme().getName(),
                waiting.getDate(),
                waiting.getTime().getStartAt(),
                rank + "번째 예약대기");
    }

    public static List<MemberReservationResponse> from(List<Reservation> reservations, List<WaitingWithRank> waitingWithRank) {
        List<MemberReservationResponse> reservationResponses = reservations.stream()
                .map(MemberReservationResponse::from)
                .toList();
        List<MemberReservationResponse> waitingResponses = waitingWithRank.stream()
                .map(MemberReservationResponse::from)
                .toList();
        return Stream.concat(reservationResponses.stream(), waitingResponses.stream()).toList();
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
