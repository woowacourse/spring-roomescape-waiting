package roomescape.adapter.web.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDate;
import roomescape.application.dto.result.MyReservationResult;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class MyReservationResponse {

    public enum Status {RESERVED, WAITING}

    private final Long id;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private final LocalDate date;
    private final ReservationTimeResponse time;
    private final ThemeResponse theme;
    private final Status status;
    private final Integer waitingOrder;
    private final String paymentStatus;
    private final String orderId;
    private final String paymentKey;
    private final Long amount;

    public MyReservationResponse(Long id, LocalDate date, ReservationTimeResponse time,
                                 ThemeResponse theme, Status status, Integer waitingOrder,
                                 String paymentStatus, String orderId, String paymentKey, Long amount) {
        this.id = id;
        this.date = date;
        this.time = time;
        this.theme = theme;
        this.status = status;
        this.waitingOrder = waitingOrder;
        this.paymentStatus = paymentStatus;
        this.orderId = orderId;
        this.paymentKey = paymentKey;
        this.amount = amount;
    }

    public static MyReservationResponse from(MyReservationResult r) {
        return new MyReservationResponse(
                r.getId(),
                r.getDate(),
                ReservationTimeResponse.from(r.getTime()),
                ThemeResponse.from(r.getTheme()),
                Status.valueOf(r.getStatus().name()),
                r.getWaitingOrder(),
                r.getPaymentStatus() == null ? null : r.getPaymentStatus().name(),
                r.getOrderId(),
                r.getPaymentKey(),
                r.getPaymentAmount()
        );
    }

    public Long getId() {
        return id;
    }

    public LocalDate getDate() {
        return date;
    }

    public ReservationTimeResponse getTime() {
        return time;
    }

    public ThemeResponse getTheme() {
        return theme;
    }

    public Status getStatus() {
        return status;
    }

    public Integer getWaitingOrder() {
        return waitingOrder;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getPaymentKey() {
        return paymentKey;
    }

    public Long getAmount() {
        return amount;
    }
}
