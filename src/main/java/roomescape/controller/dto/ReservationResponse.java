package roomescape.controller.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import roomescape.domain.ReservationStatus;
import roomescape.service.dto.ReservationResult;

public class ReservationResponse {
    private final Long id;
    private final String name;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private final LocalDate date;

    private final ReservationTimeResponse time;
    private final ThemeResponse theme;
    private final ReservationStatus status;

    public ReservationResponse(Long id, String name, LocalDate date, ReservationTimeResponse time,
                               ThemeResponse theme, ReservationStatus status) {
        this.id = id;
        this.name = name;
        this.date = date;
        this.time = time;
        this.theme = theme;
        this.status = status;
    }

    public static ReservationResponse from(ReservationResult result) {
        return new ReservationResponse(
                result.getId(),
                result.getName(),
                result.getDate(),
                ReservationTimeResponse.from(result.getTime()),
                ThemeResponse.from(result.getTheme()),
                result.getStatus()
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

    public ReservationStatus getStatus() {
        return status;
    }
}
