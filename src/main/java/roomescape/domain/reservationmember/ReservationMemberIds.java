package roomescape.domain.reservationmember;

public class ReservationMemberIds {

    private final long id;
    private final long reservationId;
    private final long memberId;

    public ReservationMemberIds(long id, long reservationId, long memberId) {
        this.id = id;
        this.reservationId = reservationId;
        this.memberId = memberId;
    }

    public long getId() {
        return id;
    }

    public long getReservationId() {
        return reservationId;
    }

    public long getMemberId() {
        return memberId;
    }
}
