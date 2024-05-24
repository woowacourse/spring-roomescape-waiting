package roomescape.service.dto.response;

import java.time.LocalDate;
import roomescape.domain.Waiting;

public class WaitingResponse {

    private final Long id;
    private final LocalDate date;
    private final String name;
    private final ReservationTimeResponse time;
    private final ThemeResponse theme;

    public WaitingResponse(Long id, LocalDate date, String name, ReservationTimeResponse time,
                           ThemeResponse theme) {
        this.id = id;
        this.date = date;
        this.name = name;
        this.time = time;
        this.theme = theme;
    }

    public WaitingResponse(Waiting waiting) {
        this(waiting.getId(),
                waiting.getDate(),
                waiting.getMember().getName(),
                new ReservationTimeResponse(waiting.getTime()),
                new ThemeResponse(waiting.getTheme()));
    }

    public Long getId() {
        return id;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getName() {
        return name;
    }

    public ReservationTimeResponse getTime() {
        return time;
    }

    public ThemeResponse getTheme() {
        return theme;
    }
}
