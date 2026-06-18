package roomescape.domain.payment;

import java.time.LocalDate;

public class Order {

    private final String orderId;
    private final Long amount;
    private final String name;
    private final LocalDate date;
    private final Long timeId;
    private final Long themeId;
    private final String idempotencyKey;

    public Order(String orderId, Long amount, String name, LocalDate date, Long timeId, Long themeId, String idempotencyKey) {
        this.orderId = orderId;
        this.amount = amount;
        this.name = name;
        this.date = date;
        this.timeId = timeId;
        this.themeId = themeId;
        this.idempotencyKey = idempotencyKey;
    }

    public String getOrderId() {
        return orderId;
    }

    public Long getAmount() {
        return amount;
    }

    public String getName() {
        return name;
    }

    public LocalDate getDate() {
        return date;
    }

    public Long getTimeId() {
        return timeId;
    }

    public Long getThemeId() {
        return themeId;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }
}