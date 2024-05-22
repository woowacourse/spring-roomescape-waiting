package roomescape.domain;

public enum ReservationStatus {
    RESERVED("예약"),
    WAITING("예약 대기"),
    ;

    private final String message;

    ReservationStatus(String message) {
        this.message = message;
    }

    public static String messageOf(Reservation reservation, Member member) {
        if (reservation.isOwn(member)) {
            return RESERVED.message;
        }
        return member.getReservationRanking(reservation) + "번째 " + WAITING.message;
    }
}
