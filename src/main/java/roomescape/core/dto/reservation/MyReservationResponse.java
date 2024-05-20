package roomescape.core.dto.reservation;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import roomescape.core.domain.Reservation;
import roomescape.core.domain.ReservationTime;
import roomescape.core.domain.Theme;
import roomescape.core.domain.Waiting;
import roomescape.core.domain.WaitingWithRank;

public class MyReservationResponse {
    private final Long reservationId;
    private final String theme;
    private final String date;
    private final String time;
    private final String status;

    private MyReservationResponse(final Long reservationId, final String theme, final String date, final String time,
                                  final String status) {
        this.reservationId = reservationId;
        this.theme = theme;
        this.date = date;
        this.time = time;
        this.status = status;
    }

    public static MyReservationResponse from(final WaitingWithRank waitingWithRank) {
        final Waiting waiting = waitingWithRank.getWaiting();
        final Theme theme = waiting.getTheme();
        final LocalDate date = waiting.getDate();
        final ReservationTime time = waiting.getTime();
        final String status = (waitingWithRank.getRank() + 1) + "번째 예약 대기";

        return new MyReservationResponse(waiting.getId(), theme.getName(), date.format(DateTimeFormatter.ISO_DATE),
                time.getStartAtString(), status);
    }

    public static MyReservationResponse from(final Reservation reservation) {
        final Theme theme = reservation.getTheme();
        final ReservationTime time = reservation.getReservationTime();
        final String status = "예약";

        return new MyReservationResponse(reservation.getId(), theme.getName(), reservation.getDateString(),
                time.getStartAtString(), status);
    }

    public Long getReservationId() {
        return reservationId;
    }

    public String getTheme() {
        return theme;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public String getStatus() {
        return status;
    }
}
