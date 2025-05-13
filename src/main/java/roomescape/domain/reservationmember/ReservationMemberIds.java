package roomescape.domain.reservationmember;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class ReservationMemberIds {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private long reservationId;
    private long memberId;

    public ReservationMemberIds() {

    }

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
