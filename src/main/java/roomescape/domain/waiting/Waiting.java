package roomescape.domain.waiting;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import roomescape.domain.reservation.Reservation;

@Entity
public class Waiting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    private Reservation reservation;

    @Embedded
    private WaitingOrder waitingOrder;

    protected Waiting() {
    }

    public Waiting(Reservation reservation, int waitingOrder) {
        validateAvailableWaiting(reservation);
        this.reservation = reservation;
        this.waitingOrder = new WaitingOrder(waitingOrder);
    }

    public void decreaseWaitingOrderByOne() {
        waitingOrder.decreaseWaitingOrderByOne();
    }

    public boolean isFirstOrder() {
        return waitingOrder.isFirstOrder();
    }

    private void validateAvailableWaiting(Reservation reservation) {
        if (reservation.isReserved()) {
            throw new IllegalArgumentException(
                    "[ERROR] 확정된 예약은 대기가 불가능합니다.",
                    new Throwable("reservation_id : " + reservation.getId())
            );
        }
    }

    public Long getId() {
        return id;
    }

    public Reservation getReservation() {
        return reservation;
    }

    public int getWaitingOrderValue() {
        return waitingOrder.getWaitingOrder();
    }
}
