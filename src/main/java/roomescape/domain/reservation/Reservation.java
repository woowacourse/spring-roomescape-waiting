package roomescape.domain.reservation;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import roomescape.domain.reservationslot.ReservationSlot;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.reservationwaiting.ReservationWaiting;
import roomescape.domain.reservationwaiting.ReservationWaitingLine;
import roomescape.domain.theme.Theme;

public class Reservation {
    private static final String PAST_RESERVATION_MESSAGE = "과거 날짜와 시간으로는 예약을 할 수 없습니다.";
    private static final String PAST_CANCEL_MESSAGE = "이미 지난 예약은 취소할 수 없습니다.";
    private static final String PAST_UPDATE_MESSAGE = "이미 지난 예약은 변경할 수 없습니다.";
    private static final String DIFFERENT_SLOT_MESSAGE = "예약과 대기 줄의 슬롯이 일치하지 않습니다.";

    private final Long id;
    private final ReservationName name;
    private final ReservationSlot slot;
    private final LocalDateTime createdAt;

    public Reservation(
            final Long id,
            final String name,
            final ReservationSlot slot,
            final LocalDateTime createdAt
    ) {
        this(id, ReservationName.from(name), slot, createdAt);
    }

    public Reservation(
            final Long id,
            final ReservationName name,
            final ReservationSlot slot,
            final LocalDateTime createdAt
    ) {
        validateId(id);
        validateName(name);
        validateSlot(slot);
        validate(createdAt);
        this.id = id;
        this.name = name;
        this.slot = slot;
        this.createdAt = createdAt;
    }

    public static Reservation reserve(
            final String name,
            final ReservationSlot slot,
            final LocalDateTime standardDateTime
    ) {
        return reserve(ReservationName.from(name), slot, standardDateTime);
    }

    public static Reservation reserve(
            final ReservationName name,
            final ReservationSlot slot,
            final LocalDateTime standardDateTime
    ) {
        validateName(name);
        validateReservable(slot, standardDateTime);
        validate(standardDateTime);
        return new Reservation(name, slot, standardDateTime);
    }

    private Reservation(
            final ReservationName name,
            final ReservationSlot slot,
            final LocalDateTime createdAt
    ) {
        validateName(name);
        validateSlot(slot);
        validate(createdAt);
        this.id = null;
        this.name = name;
        this.slot = slot;
        this.createdAt = createdAt;
    }

    public Reservation withId(final Long id) {
        validateId(id);
        return new Reservation(id, this.name, this.slot, this.createdAt);
    }

    public Reservation withSlot(
            final ReservationSlot slot,
            final LocalDateTime standardDateTime
    ) {
        validateReservable(slot, standardDateTime);
        return new Reservation(this.id, this.name, slot, this.createdAt);
    }

    public ReservationCancellationResult cancel(
            final ReservationWaitingLine waitingLine,
            final LocalDateTime requestedAt
    ) {
        validateWaitingLine(waitingLine);

        return waitingLine.first()
                .map(waiting -> promote(waiting, requestedAt))
                .orElseGet(() -> ReservationCancellationResult.withoutPromotion(this));
    }

    public boolean hasName(final String name) {
        return this.name.equals(ReservationName.from(name));
    }

    public boolean isPast(final LocalDateTime standardDateTime) {
        return slot.isPast(standardDateTime);
    }

    public void validateCancelable(final LocalDateTime standardDateTime) {
        if (isPast(standardDateTime)) {
            throw new PastReservationException(PAST_CANCEL_MESSAGE);
        }
    }

    public void validateUpdatable(final LocalDateTime standardDateTime) {
        if (isPast(standardDateTime)) {
            throw new PastReservationException(PAST_UPDATE_MESSAGE);
        }
    }

    private static void validateReservable(
            final ReservationSlot slot,
            final LocalDateTime standardDateTime
    ) {
        validateSlot(slot);
        if (slot.isPast(standardDateTime)) {
            throw new PastReservationException(PAST_RESERVATION_MESSAGE);
        }
    }

    private ReservationCancellationResult promote(
            final ReservationWaiting waiting,
            final LocalDateTime requestedAt
    ) {
        return ReservationCancellationResult.withPromotion(
                this,
                waiting,
                waiting.toReservation(requestedAt)
        );
    }

    private void validateWaitingLine(final ReservationWaitingLine waitingLine) {
        if (waitingLine == null) {
            throw new IllegalArgumentException("대기 줄은 비어있으면 안됩니다.");
        }
        if (!waitingLine.isEmpty() && !waitingLine.isForSlot(this.slot)) {
            throw new IllegalArgumentException(DIFFERENT_SLOT_MESSAGE);
        }
    }

    private static void validateSlot(final ReservationSlot slot) {
        if (slot == null) {
            throw new IllegalArgumentException("예약 슬롯은 비어있으면 안됩니다.");
        }
    }

    private static void validateName(final ReservationName name) {
        if (name == null) {
            throw new IllegalArgumentException("예약자 이름은 필수입니다.");
        }
    }

    private static void validateId(final Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Id는 비어있을 수 없습니다.");
        }
    }

    private static void validate(final LocalDateTime createdAt) {
        if (createdAt == null) {
            throw new IllegalArgumentException("예약 생성 시각은 비어있으면 안됩니다.");
        }
    }

    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof Reservation)) {
            return false;
        }
        Reservation r = (Reservation) o;
        return Objects.equals(id, r.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public Long getId() {
        return this.id;
    }

    public String getName() {
        return this.name.value();
    }

    public LocalDate getDate() {
        return this.slot.getDate();
    }

    public Theme getTheme() {
        return this.slot.getTheme();
    }

    public ReservationTime getTime() {
        return this.slot.getTime();
    }

    public ReservationSlot getSlot() {
        return this.slot;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }
}
