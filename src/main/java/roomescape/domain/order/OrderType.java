package roomescape.domain.order;

public enum OrderType {
    RESERVATION("예약"),
    WAITING("대기");

    private final String label;

    OrderType(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }
}
