package roomescape.domain.order;

public class Order {

    private final String id;
    private final long amount;
    private final long reservationId;

    public Order(String id, long amount, long reservationId) {
        this.id = id;
        this.amount = amount;
        this.reservationId = reservationId;
    }

    public String getId() {
        return id;
    }

    public long getAmount() {
        return amount;
    }

    public long getReservationId() {
        return reservationId;
    }
}
