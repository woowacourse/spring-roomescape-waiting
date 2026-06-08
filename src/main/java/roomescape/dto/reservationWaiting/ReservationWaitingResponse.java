package roomescape.dto.reservationWaiting;

import roomescape.domain.reservatinWaiting.ReservationWaiting;
import roomescape.dto.reservationtime.ReservationTimeResponse;
import roomescape.dto.theme.ThemeResponse;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class ReservationWaitingResponse {

    private final Long id;
    private final String name;
    private final LocalDate date;
    private final ReservationTimeResponse time;
    private final ThemeResponse theme;
    private final Long sequence;
    private final LocalDateTime createdAt;

    public ReservationWaitingResponse(Long id, String name, LocalDate date, ReservationTimeResponse time, ThemeResponse theme, Long sequence, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.date = date;
        this.time = time;
        this.theme = theme;
        this.sequence = sequence;
        this.createdAt = createdAt;
    }

    public static ReservationWaitingResponse from(ReservationWaiting reservationWaiting, Long sequence) {
        ReservationTimeResponse reservationTimeResponse = ReservationTimeResponse.from(reservationWaiting.getTime());
        ThemeResponse themeResponse = ThemeResponse.from(reservationWaiting.getTheme());
        return new ReservationWaitingResponse(reservationWaiting.getId(), reservationWaiting.getName(), reservationWaiting.getDate(), reservationTimeResponse, themeResponse,
                sequence, reservationWaiting.getCreatedAt());
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

    public Long getSequence() {
        return sequence;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
