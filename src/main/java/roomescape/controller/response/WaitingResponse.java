package roomescape.controller.response;

import roomescape.model.Waiting;

import java.time.LocalDate;

public class WaitingResponse {

    private Long id;
    private String name;
    private LocalDate date;
    private ReservationTimeResponse time;
    private ReservationThemeResponse theme;

    private WaitingResponse() {
    }

    public WaitingResponse(Waiting waiting) {
        this.id = waiting.getId();
        this.name = waiting.getMember().getName();
        this.date = waiting.getDate();
        this.time = ReservationTimeResponse.of(waiting.getTime());
        this.theme = ReservationThemeResponse.of(waiting.getTheme());
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

    public ReservationThemeResponse getTheme() {
        return theme;
    }

}
