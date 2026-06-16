package roomescape.payment.domain;

import lombok.Getter;
import roomescape.global.exception.InvalidRequestException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.regex.Pattern;

@Getter
public class PaymentOrder {
    private static final Pattern ORDER_ID_PATTERN = Pattern.compile("^[A-Za-z0-9_-]{6,64}$");

    private final Long id;
    private final String orderId;
    private final long amount;
    private final PaymentOrderStatus status;
    private final String name;
    private final LocalDate date;
    private final Long timeId;
    private final Long themeId;
    private final String paymentKey;
    private final Long reservationId;
    private final String failureCode;
    private final String failureMessage;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private final LocalDateTime confirmedAt;

    public static PaymentOrder ready(
            String orderId,
            long amount,
            String name,
            LocalDate date,
            Long timeId,
            Long themeId,
            LocalDateTime now
    ) {
        return new PaymentOrder(
                null,
                orderId,
                amount,
                PaymentOrderStatus.READY,
                name,
                date,
                timeId,
                themeId,
                null,
                null,
                null,
                null,
                now,
                now,
                null
        );
    }

    public PaymentOrder(
            Long id,
            String orderId,
            long amount,
            PaymentOrderStatus status,
            String name,
            LocalDate date,
            Long timeId,
            Long themeId,
            String paymentKey,
            Long reservationId,
            String failureCode,
            String failureMessage,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            LocalDateTime confirmedAt
    ) {
        validate(orderId, amount, status, name, date, timeId, themeId, createdAt, updatedAt);
        this.id = id;
        this.orderId = orderId;
        this.amount = amount;
        this.status = status;
        this.name = name;
        this.date = date;
        this.timeId = timeId;
        this.themeId = themeId;
        this.paymentKey = paymentKey;
        this.reservationId = reservationId;
        this.failureCode = failureCode;
        this.failureMessage = failureMessage;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.confirmedAt = confirmedAt;
    }

    public PaymentOrder withId(Long id) {
        if (id == null) {
            throw new InvalidRequestException("결제 주문 id는 비어 있을 수 없습니다.");
        }
        if (this.id != null) {
            throw new InvalidRequestException("이미 식별자가 존재하는 결제 주문입니다.");
        }

        return new PaymentOrder(
                id,
                orderId,
                amount,
                status,
                name,
                date,
                timeId,
                themeId,
                paymentKey,
                reservationId,
                failureCode,
                failureMessage,
                createdAt,
                updatedAt,
                confirmedAt
        );
    }

    public PaymentOrder confirm(String paymentKey, Long reservationId, LocalDateTime now) {
        if (paymentKey == null || paymentKey.isBlank()) {
            throw new InvalidRequestException("결제 키는 비어 있을 수 없습니다.");
        }
        if (reservationId == null) {
            throw new InvalidRequestException("결제된 예약 id는 비어 있을 수 없습니다.");
        }

        return new PaymentOrder(
                id,
                orderId,
                amount,
                PaymentOrderStatus.CONFIRMED,
                name,
                date,
                timeId,
                themeId,
                paymentKey,
                reservationId,
                null,
                null,
                createdAt,
                now,
                now
        );
    }

    public PaymentOrder fail(String code, String message, LocalDateTime now) {
        return new PaymentOrder(
                id,
                orderId,
                amount,
                PaymentOrderStatus.FAILED,
                name,
                date,
                timeId,
                themeId,
                paymentKey,
                reservationId,
                code,
                message,
                createdAt,
                now,
                confirmedAt
        );
    }

    public boolean isReady() {
        return status == PaymentOrderStatus.READY;
    }

    public boolean isConfirmed() {
        return status == PaymentOrderStatus.CONFIRMED;
    }

    public boolean hasAmount(long amount) {
        return this.amount == amount;
    }

    private void validate(
            String orderId,
            long amount,
            PaymentOrderStatus status,
            String name,
            LocalDate date,
            Long timeId,
            Long themeId,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        if (orderId == null || !ORDER_ID_PATTERN.matcher(orderId).matches()) {
            throw new InvalidRequestException("주문번호는 6~64자의 영문, 숫자, -, _만 사용할 수 있습니다.");
        }
        if (amount <= 0) {
            throw new InvalidRequestException("결제 금액은 1원 이상이어야 합니다.");
        }
        if (status == null) {
            throw new InvalidRequestException("결제 주문 상태는 비어 있을 수 없습니다.");
        }
        if (name == null || name.isBlank()) {
            throw new InvalidRequestException("예약자 이름은 비어 있을 수 없습니다.");
        }
        if (date == null) {
            throw new InvalidRequestException("예약 날짜는 비어 있을 수 없습니다.");
        }
        if (timeId == null) {
            throw new InvalidRequestException("예약 시간 id는 비어 있을 수 없습니다.");
        }
        if (themeId == null) {
            throw new InvalidRequestException("테마 id는 비어 있을 수 없습니다.");
        }
        if (createdAt == null || updatedAt == null) {
            throw new InvalidRequestException("결제 주문 시각은 비어 있을 수 없습니다.");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PaymentOrder that)) return false;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
