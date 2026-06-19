package roomescape.domain.payment;

import java.time.LocalDate;

public class Order {

    private final String orderId;
    private final Long amount;
    private final String name;
    private final LocalDate date;
    private final Long timeId;
    private final Long themeId;

    public Order(String orderId, Long amount, String name, LocalDate date, Long timeId, Long themeId) {
        this.orderId = orderId;
        this.amount = amount;
        this.name = name;
        this.date = date;
        this.timeId = timeId;
        this.themeId = themeId;
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
}