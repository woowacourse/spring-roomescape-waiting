package roomescape.domain.reservation;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import roomescape.common.exception.BadRequestException;
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

    public Reservation(Long id, UserName userName, EventSlot eventSlot, ReservationStatus status) {
        this(id, userName, eventSlot.date(), eventSlot.time(), eventSlot.theme(), status);
    }

    public static Reservation restore(Long id, UserName userName, LocalDate date, ReservationTime time, Theme theme,
                                      ReservationStatus status) {
        return new Reservation(id, userName, date, time, theme, status);
    }

    public static Reservation restore(Long id, UserName userName, EventSlot eventSlot, ReservationStatus status) {
        return new Reservation(id, userName, eventSlot.date(), eventSlot.time(), eventSlot.theme(), status);
    }

    public static Reservation restoreConfirmed(Long id, UserName userName, EventSlot eventSlot) {
        return new Reservation(id, userName, eventSlot.date(), eventSlot.time(), eventSlot.theme(),
                ReservationStatus.CONFIRMED);
    }

    public static Reservation createPending(UserName userName, LocalDate date, ReservationTime time, Theme theme) {
        return new Reservation(null, userName, date, time, theme, ReservationStatus.PENDING);
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
        if (this.status == ReservationStatus.CANCELED || this.status == ReservationStatus.REJECTED) {
            throw new BadRequestException("변경할 수 없는 예약입니다.");
        }

        validateOwner(userName, "다른 사람의 예약은 변경할 수 없습니다.");

        EventSlot newEventSlot = EventSlot.from(newDate, newTime, this.eventSlot.theme());
        validatePast(newEventSlot, now, "과거의 시간으로 예약을 변경할 수 없습니다.");

        return new Reservation(this.id, userName, newEventSlot, status.pending());
    }

    public Reservation cancel(UserName userName, LocalDateTime now) {
        if (this.status == ReservationStatus.CANCELED || this.status == ReservationStatus.REJECTED) {
            throw new BadRequestException("취소할 수 없는 예약입니다.");
        }

        validateOwner(userName, "다른 사람의 예약은 취소할 수 없습니다.");
        validatePast(eventSlot, now, "이미 지난 예약은 취소할 수 없습니다.");

        return new Reservation(this.id, this.userName, this.eventSlot, this.status.cancel());
    }

    public Reservation cancel() {
        if (this.status == ReservationStatus.CONFIRMED || this.status == ReservationStatus.PENDING) {
            throw new BadRequestException("취소할 수 없는 예약입니다.");
        }

        return new Reservation(this.id, this.userName, this.eventSlot, this.status.cancel());
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
        if (this.status != ReservationStatus.PENDING) {
            throw new BadRequestException("확정할 수 없는 예약입니다.");
        }

        return new Reservation(id, userName, eventSlot, status.confirm());
    }

    public Reservation reject() {
        if (this.status != ReservationStatus.PENDING) {
            throw new BadRequestException("확정할 수 없는 예약입니다.");
        }

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

    public ReservationStatus getStatus() {
        return status;
    }
}
