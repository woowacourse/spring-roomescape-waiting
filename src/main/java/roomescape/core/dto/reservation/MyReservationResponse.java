package roomescape.core.dto.reservation;

public class MyReservationResponse {
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

    public static MyReservationResponse ofReservationWaiting(final Long reservationId, final String theme,
                                                             final String date, final String time,
                                                             final String status, final Integer rank) {
        return new MyReservationResponse(reservationId, theme, date, time, waitingRankStatus(status, rank));
    }

    public static MyReservationResponse ofReservation(final Long reservationId, final String theme,
                                                      final String date, final String time, final String status) {
        return new MyReservationResponse(reservationId, theme, date, time, status);
    }

    private static String waitingRankStatus(final String status, final Integer rank) {
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
