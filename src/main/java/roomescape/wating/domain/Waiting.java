package roomescape.wating.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import lombok.Getter;
import roomescape.reservation.domain.CustomerName;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;
import roomescape.wating.domain.exception.PastDateTimeWaitingException;

@Getter
public class Waiting {

    private final Long id;
    private final CustomerName customerName;
    private final LocalDate reservationDate;
    private final LocalDateTime createdAt;
    private final ReservationTime time;
    private final Theme theme;

    private Waiting(
            Long id,
            CustomerName customerName,
            LocalDate reservationDate,
            LocalDateTime createdAt,
            ReservationTime time,
            Theme theme
    ) {
        validateNotNull(customerName, reservationDate, time, theme);
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
                CustomerName.from(customerName),
                date,
                null,
                time,
                theme);

        waiting.validateNotPast(now);
        return waiting;
    }

    private void validateNotNull(
            final CustomerName customerName,
            final LocalDate reservationDate,
            final ReservationTime time,
            final Theme theme
    ) {
        try {
            Objects.requireNonNull(customerName);
            Objects.requireNonNull(reservationDate);
            Objects.requireNonNull(time);
            Objects.requireNonNull(theme);
        } catch (NullPointerException exception) {
            throw new IllegalStateException("예약자명/예약대기날짜/시간/테마는 필수 입력값입니다.");
        }
    }

    private void validateNotPast(final LocalDateTime now) {
        if (isPast(now)) {
            throw new PastDateTimeWaitingException();
        }
    }

    private boolean isPast(final LocalDateTime now) {
        return reservationDateTime().isBefore(now);
    }

    private LocalDateTime reservationDateTime() {
        return LocalDateTime.of(reservationDate, time.getStartAt());
    }
}
