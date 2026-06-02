package roomescape.controller.dto.response;

import java.time.LocalDate;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationResult;

public class ReservationResponse {
    private final long id;
    private final String name;
    private final LocalDate date;
    private final String state;
    private final int rank;
    private final ReservationTimeResponse time;
    private final ThemeResponse theme;

    public ReservationResponse(long id, String name, LocalDate date, String state, int rank,
                               ReservationTimeResponse time,
                               ThemeResponse theme) {
        this.id = id;
        this.name = name;
        this.date = date;
        this.state = state;
        this.rank = rank;
        this.time = time;
        this.theme = theme;
    }

    public static ReservationResponse from(ReservationResult reservationResult) {
        Reservation reservation = reservationResult.getReservation();
        return new ReservationResponse(reservation.getId(), reservation.getName().getValue(),
                reservation.getDate().getValue(),
                reservationResult.getReservation().getStatus().getKoreanName(),
                reservationResult.getRank().getValue(),
                ReservationTimeResponse.from(reservation.getTime()),
                ThemeResponse.from(reservation.getTheme()));
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public LocalDate getDate() {
        return date;
    }

    public ReservationTimeResponse getTime() {
        return time;
    }

    public ThemeResponse getTheme() {
        return theme;
    }

    public String getState() {
        return state;
    }

    public int getRank() {
        return rank;
    }
}
