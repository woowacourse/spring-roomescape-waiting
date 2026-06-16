package roomescape.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import roomescape.exception.InvalidStateException;

public class Reservation {
    private Long id;
    private String name;
    private LocalDate date;
    private ReservationTime time;
    private Theme theme;
    private ReservationStatus status;
    private String orderId;
    private String paymentKey;
    private Long amount;

    public Reservation(String name, LocalDate date, ReservationTime time, Theme theme, ReservationStatus status) {
        this(null, name, date, time, theme, status, null, null, null);
    }

    public Reservation(Long id, String name, LocalDate date, ReservationTime time, Theme theme,
                       ReservationStatus status, String orderId, String paymentKey, Long amount) {
        this.id = id;
        this.status = status;
        this.orderId = orderId;
        this.paymentKey = paymentKey;
        this.amount = amount;
        validateName(name);
        validateDate(date);
        validateTime(time);
        this.name = name;
        this.date = date;
        this.time = time;
        this.theme = theme;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public LocalDate getDate() {
        return date;
    }

    public ReservationTime getTime() {
        return time;
    }

    public Theme getTheme() {
        return theme;
    }

    public ReservationStatus getStatus() {
        return status;
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

    public void completePayment(String paymentKey) {
        this.paymentKey = paymentKey;
        this.status = ReservationStatus.CONFIRMED;
    }

    public void setPaymentInfo(String orderId, Long amount) {
        this.orderId = orderId;
        this.amount = amount;
        this.status = ReservationStatus.PENDING_PAYMENT;
    }

    public boolean isSameDateTime(LocalDate date, Long timeId) {
        return this.date.equals(date) && this.time.getId().equals(timeId);
    }

    public boolean isConfirmed() {
        return status == ReservationStatus.CONFIRMED;
    }

    public boolean isPendingPayment() {
        return status == ReservationStatus.PENDING_PAYMENT;
    }

    public boolean takesSlot() {
        return isConfirmed() || isPendingPayment();
    }

    public boolean isWaiting() {
        return status == ReservationStatus.WAITING;
    }

    public boolean isToday() {
        return date.equals(LocalDate.now());
    }

    public void validateNotPast() {
        LocalDateTime targetDateTime = LocalDateTime.of(this.date, this.time.getStartAt());
        if (targetDateTime.isBefore(LocalDateTime.now())) {
            throw new InvalidStateException("이미 지난 시간/날짜는 예약할 수 없습니다.");
        }
    }

    private void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("예약자명이 유효하지 않습니다.");
        }
    }

    private void validateDate(LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("예약 날짜가 유효하지 않습니다.");
        }
    }

    private void validateTime(ReservationTime time) {
        if (time == null) {
            throw new IllegalArgumentException("예약 시간이 유효하지 않습니다.");
        }
    }
}
