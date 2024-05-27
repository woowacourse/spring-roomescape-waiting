package roomescape.service.dto.reservation;

import roomescape.domain.reservation.Reservation;

public class ReservationResponse {

    private final long id;
    private final String name;
    private final String theme;
    private final String date;
    private final String startAt;
    private final String status;

    public ReservationResponse(long id,
                               String name,
                               String theme,
                               String date,
                               String startAt,
                               String status) {
        this.id = id;
        this.name = name;
        this.theme = theme;
        this.date = date;
        this.startAt = startAt;
        this.status = status;
    }

    public ReservationResponse(Reservation reservation) {
        this(reservation.getId(),
                reservation.memberName(),
                reservation.themeName(),
                reservation.getDate().toString(),
                reservation.getStartAt().toString(),
                reservation.getReservationStatus().toString());
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getTheme() {
        return theme;
    }

    public String getDate() {
        return date;
    }

    public String getStartAt() {
        return startAt;
    }

    public String getStatus() {
        return status;
    }
}
