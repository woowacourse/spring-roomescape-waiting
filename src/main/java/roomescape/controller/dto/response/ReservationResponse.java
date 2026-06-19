package roomescape.controller.dto.response;

import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.Slot;

import java.time.LocalDate;

public class ReservationResponse {
    private final long id;
    private final String name;
    private final LocalDate date;
    private final String state;
    private final Long rank;
    private final ReservationTimeResponse time;
    private final ThemeResponse theme;

    public ReservationResponse(long id, String name, LocalDate date, String state, Long rank,
                               ReservationTimeResponse time, ThemeResponse theme) {
        this.id = id;
        this.name = name;
        this.date = date;
        this.state = state;
        this.rank = rank;
        this.time = time;
        this.theme = theme;
    }

    public static ReservationResponse toDto(Reservation reservation) {
        Slot slot = reservation.getSlot();
        Long rank = reservation.getRank() != null ? reservation.getRank().getValue() : null;
        return new ReservationResponse(
                reservation.getId(),
                reservation.getMember().getName(),
                slot.getDate().getDate(),
                reservation.getStatus().getKoreanName(),
                rank,
                ReservationTimeResponse.toDto(slot.getTime()),
                ThemeResponse.toDto(slot.getTheme()));
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

    public String getState() {
        return state;
    }

    public Long getRank() {
        return rank;
    }

    public ReservationTimeResponse getTime() {
        return time;
    }

    public ThemeResponse getTheme() {
        return theme;
    }
}
