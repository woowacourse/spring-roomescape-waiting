package roomescape.domain.reservation;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import roomescape.common.exception.ForbiddenException;
import roomescape.common.exception.UnprocessableEntityException;
import roomescape.domain.slot.EventSlot;
import roomescape.domain.slot.theme.Theme;
import roomescape.domain.slot.time.ReservationTime;

public class Reservation {

    private final Long id;
    private final UserName userName;
    private final EventSlot eventSlot;
    private ReservationStatus status;

    public Reservation(UserName userName, LocalDate date, ReservationTime time, Theme theme) {
        this(null, userName, date, time, theme, ReservationStatus.PENDING);
    }

    public Reservation(Long id, UserName userName, LocalDate date, ReservationTime time, Theme theme) {
        this(id, userName, date, time, theme, ReservationStatus.PENDING);
    }

    public Reservation(Long id, UserName userName, EventSlot eventSlot) {
        this(id, userName, eventSlot.date(), eventSlot.time(), eventSlot.theme());
    }

    public Reservation(Long id, UserName userName, EventSlot eventSlot, ReservationStatus status) {
        this(id, userName, eventSlot.date(), eventSlot.time(), eventSlot.theme(), status);
    }

    public Reservation(
            Long id,
            UserName userName,
            LocalDate date,
            ReservationTime time,
            Theme theme,
            ReservationStatus status
    ) {
        this.id = id;
        validate(userName, date, time, theme);
        this.userName = userName;
        this.eventSlot = EventSlot.from(date, time, theme);
        this.status = status;
    }

    private void validate(UserName userName, LocalDate date, ReservationTime time, Theme theme) {
        Objects.requireNonNull(userName, "예약자 이름이 비어 있습니다.");
        Objects.requireNonNull(date, "예약 날짜가 비어 있습니다.");
        Objects.requireNonNull(time, "시간이 비어 있습니다.");
        Objects.requireNonNull(theme, "테마가 비어 있습니다.");
    }

    public void verifyBookable(LocalDateTime now) {
        LocalDate today = now.toLocalDate();

        if (eventSlot.isBeforeDate(today)) {
            status = status.reject();
            throw new UnprocessableEntityException("과거 날짜로는 예약할 수 없습니다.");
        }

        if (eventSlot.isEqualDate(today) && eventSlot.isBeforeTime(now.toLocalTime())) {
            status = status.reject();
            throw new UnprocessableEntityException("이미 지난 시간으로 예약할 수 없습니다.");
        }
    }

    public Reservation change(UserName userName, LocalDate newDate, ReservationTime newTime, LocalDateTime now) {
        validateOwner(userName, "다른 사람의 예약은 변경할 수 없습니다.");

        EventSlot newEventSlot = EventSlot.from(newDate, newTime, this.eventSlot.theme());
        validatePast(newEventSlot, now, "과거의 시간으로 예약을 변경할 수 없습니다.");

        return new Reservation(this.id, userName, newEventSlot, status.pending());
    }

    public void cancel(UserName userName, LocalDateTime now) {
        validateOwner(userName, "다른 사람의 예약은 취소할 수 없습니다.");
        validatePast(eventSlot, now, "이미 지난 예약은 취소할 수 없습니다.");

        status = status.cancel();
    }

    public void cancel() {
        status = status.cancel();
    }

    private void validateOwner(UserName userName, String message) {
        if (!userName.equals(this.userName)) {
            throw new ForbiddenException(message);
        }
    }

    private void validatePast(EventSlot eventSlot, LocalDateTime now, String message) {
        if (eventSlot.isBeforeDateTime(now)) {
            throw new UnprocessableEntityException(message);
        }
    }

    public Reservation confirm() {
        return new Reservation(id, userName, eventSlot, status.confirm());
    }

    public Reservation reject() {
        return new Reservation(id, userName, eventSlot, status.reject());
    }

    public Long getId() {
        return id;
    }

    public UserName getName() {
        return userName;
    }

    public EventSlot getEventSlot() {
        return eventSlot;
    }
}
