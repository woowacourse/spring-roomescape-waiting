package roomescape.service.dto.response;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.domain.Reservation;
import roomescape.domain.Waiting;

public class ReservationMineResponse {
    private final Long id;
    private final ThemeResponse theme;
    private final LocalDate date;
    private final ReservationTimeResponse time;
    private final String status;
    private final Long rank;

    public ReservationMineResponse(Long id, ThemeResponse theme, LocalDate date, ReservationTimeResponse time, String status, Long rank) {
        this.id = id;
        this.theme = theme;
        this.date = date;
        this.time = time;
        this.status = status;
        this.rank = rank;
    }

    public ReservationMineResponse(Reservation reservation) {
        this(reservation.getId(),
                new ThemeResponse(reservation.getTheme()),
                reservation.getDate(),
                new ReservationTimeResponse(reservation.getTime()),
                reservation.getStatus().getDescription(),
                0L
        );
    }

    public ReservationMineResponse(Waiting waiting, Long rank) {
        this(waiting.getId(),
                new ThemeResponse(waiting.getTheme()),
                waiting.getDate(),
                new ReservationTimeResponse(waiting.getTime()),
                waiting.getStatus().getDescription(),
                rank
        );
    }

    public Long getId() {
        return id;
    }

    public ThemeResponse getTheme() {
        return theme;
    }

    public LocalDate getDate() {
        return date;
    }

    public ReservationTimeResponse getTime() {
        return time;
    }

    public String getStatus() {
        return status;
    }

    public Long getRank() {
        return rank;
    }
}
