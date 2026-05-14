package roomescape.dto.reservation;

import roomescape.domain.reservation.Reservation;
import roomescape.dto.reservationtime.ReservationTimeResponse;
import roomescape.dto.theme.ThemeResponse;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class ReservationResponse {

    private final Long id;
    private final String name;
    private final LocalDate date;
    private final ReservationTimeResponse time;
    private final ThemeResponse theme;
    private final LocalDateTime createdAt;

    private ReservationResponse(Long id, String name, LocalDate date, ReservationTimeResponse time, ThemeResponse theme, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.date = date;
        this.time = time;
        this.theme = theme;
        this.createdAt = createdAt;
    }

    public static ReservationResponse from(Reservation reservation) {
        ReservationTimeResponse reservationTimeResponse = ReservationTimeResponse.from(reservation.getTime());
        ThemeResponse themeResponse = ThemeResponse.from(reservation.getTheme());
        return new ReservationResponse(reservation.getId(), reservation.getName(), reservation.getDate(), reservationTimeResponse, themeResponse, reservation.getCreatedAt());
    }

    public Long getId() {
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
