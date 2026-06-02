package roomescape.adapter.web.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import roomescape.application.dto.result.WaitingResult;

public class WaitingResponse {
    private final Long id;
    private final String name;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private final LocalDate date;
    private final ReservationTimeResponse time;
    private final ThemeResponse theme;
    private final int orderIndex;

    public WaitingResponse(Long id, String name, LocalDate date,
                           ReservationTimeResponse time, ThemeResponse theme, int orderIndex) {
        this.id = id;
        this.name = name;
        this.date = date;
        this.time = time;
        this.theme = theme;
        this.orderIndex = orderIndex;
    }

    public static WaitingResponse from(WaitingResult r) {
        return new WaitingResponse(
                r.getId(),
                r.getName(),
                r.getDate(),
                ReservationTimeResponse.from(r.getTime()),
                ThemeResponse.from(r.getTheme()),
                r.getOrderIndex()
        );
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

    public int getOrderIndex() {
        return orderIndex;
    }
}
