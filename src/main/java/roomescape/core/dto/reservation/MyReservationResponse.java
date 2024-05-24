package roomescape.core.dto.reservation;

public class MyReservationResponse {
    private static final Integer rankOfBooked = 0;

    private Long reservationId;
    private String theme;
    private String date;
    private String time;
    private String status;

    private MyReservationResponse(final Long reservationId, final String theme, final String date, final String time,
                                  final String status) {
        this.reservationId = reservationId;
        this.theme = theme;
        this.date = date;
        this.time = time;
        this.status = status;
    }

    public static MyReservationResponse of(final Long reservationId, final String theme, final String date,
                                           final String time, final String status,
                                           final Integer rank) {
        return new MyReservationResponse(reservationId, theme, date, time, waitingRankStatus(status, rank));
    }

    private static String waitingRankStatus(final String status, final Integer rank) {
        if (rank.equals(rankOfBooked)) {
            return status;
        }
        return rank + "번째 " + status;
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
