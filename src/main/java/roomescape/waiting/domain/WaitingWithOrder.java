package roomescape.waiting.domain;

import java.util.Objects;

public class WaitingWithOrder {
    private final Waiting waiting;
    private final Long order;

    public WaitingWithOrder(Waiting waiting, Long order) {
        this.waiting = waiting;
        this.order = order;
    }

    public Waiting getWaiting() {
        return waiting;
    }

    public Long getOrder() {
        return order;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        WaitingWithOrder that = (WaitingWithOrder) o;

        if (!Objects.equals(waiting, that.waiting)) return false;
        return Objects.equals(order, that.order);
    }

    @Override
    public int hashCode() {
        int result = waiting != null ? waiting.hashCode() : 0;
        result = 31 * result + (order != null ? order.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "WaitingWithOrder{" +
               "waiting=" + waiting +
               ", order=" + order +
               '}';
    }
}
