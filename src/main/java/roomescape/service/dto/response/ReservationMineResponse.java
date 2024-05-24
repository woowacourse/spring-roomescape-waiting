package roomescape.service.dto.response;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.domain.Reservation;
import roomescape.domain.Waiting;

public class ReservationMineResponse {
    private final Long id;
    private final String theme;
    private final LocalDate date;
    private final LocalTime time;
    private final String status;

    public ReservationMineResponse(Long id, String theme, LocalDate date, LocalTime time, String status) {
        this.id = id;
        this.theme = theme;
        this.date = date;
        this.time = time;
        this.status = status;
    }

    public ReservationMineResponse(Reservation reservation) {
        this(reservation.getId(),
                reservation.getTheme().getName(),
                reservation.getDate(),
                reservation.getTime().getStartAt(),
                reservation.getStatus().getDescription()
        );
    }

    public ReservationMineResponse(Waiting waiting, Long rank) {
        this(waiting.getId(),
                waiting.getTheme().getName(),
                waiting.getDate(),
                waiting.getTime().getStartAt(),
                rank + "번째 " + waiting.getStatus().getDescription()
        );
    }

    public Long getId() {
        return id;
    }

    public String getTheme() {
        return theme;
    }

    public LocalDate getDate() {
        return date;
    }

    public LocalTime getTime() {
        return time;
    }

    public String getStatus() {
        return status;
    }
}
