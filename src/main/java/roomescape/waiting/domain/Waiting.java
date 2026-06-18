package roomescape.waiting.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import lombok.Getter;
import roomescape.reservation.domain.CustomerName;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;
import roomescape.waiting.domain.exception.PastDateTimeWaitingException;

@Getter
public class Waiting {

    private final Long id;
    private final CustomerName customerName;
    private final LocalDate reservationDate;
    private final LocalDateTime createdAt;
    private final ReservationTime time;
    private final Theme theme;

    private Waiting(
        final Long id,
        final CustomerName customerName,
        final LocalDate reservationDate,
        final LocalDateTime createdAt,
        final ReservationTime time,
        final Theme theme
    ) {
        validateNotNull(reservationDate, time, theme);
        this.id = id;
        this.customerName = customerName;
        this.reservationDate = reservationDate;
        this.createdAt = createdAt;
        this.time = time;
        this.theme = theme;
    }

    public static Waiting create(
        final String customerName,
        final LocalDate date,
        final ReservationTime time,
        final Theme theme,
        final LocalDateTime now
    ) {
        final Waiting waiting = new Waiting(
            null,
            new CustomerName(customerName),
            date,
            null,
            time,
            theme
        );

        waiting.validateNotPast(now);
        return waiting;
    }

    public static Waiting of(
        final Long id,
        final String customerName,
        final LocalDate date,
        final LocalDateTime createdAt,
        final ReservationTime time,
        final Theme theme
    ) {
        return new Waiting(
            id,
            new CustomerName(customerName),
            date,
            createdAt,
            time,
            theme
        );
    }

    public String getCustomerNameValue() {
        return customerName.name();
    }

    public long getTimeId() {
        return time.getId();
    }

    public long getThemeId() {
        return theme.getId();
    }

    public boolean isOwnedBy(final String customerName) {
        return this.customerName.equals(new CustomerName(customerName));
    }

    public boolean isCancelable(final LocalDateTime now) {
        return !isPastReservation(now);
    }

    private void validateNotNull(
        final LocalDate reservationDate,
        final ReservationTime time,
        final Theme theme
    ) {
        Objects.requireNonNull(reservationDate, "예약대기날짜는 필수 입력값입니다.");
        Objects.requireNonNull(time, "시간은 필수 입력값입니다.");
        Objects.requireNonNull(theme, "테마는 필수 입력값입니다.");
    }

    private void validateNotPast(final LocalDateTime now) {
        if (isPastReservation(now)) {
            throw new PastDateTimeWaitingException();
        }
    }

    private boolean isPastReservation(final LocalDateTime now) {
        return reservationDateTime().isBefore(now);
    }

    private LocalDateTime reservationDateTime() {
        return LocalDateTime.of(reservationDate, time.getStartAt());
    }
}
