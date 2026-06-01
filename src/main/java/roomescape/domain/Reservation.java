package roomescape.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import roomescape.domain.exception.BusinessRuleViolationException;
import roomescape.domain.exception.ForbiddenException;

public class Reservation {

    private static final String NOT_OWNER = "본인의 예약이 아닙니다.";
    private static final String PAST_RESERVATION_CREATE_REJECTED = "지난 시각에는 예약할 수 없습니다.";
    private static final String EXPIRED_RESERVATION_UPDATE_REJECTED = "이미 지난 예약은 변경할 수 없습니다.";
    private static final String PAST_RESERVATION_UPDATE_REJECTED = "지난 시각으로 예약을 변경할 수 없습니다.";
    private static final String PAST_RESERVATION_CANCEL_REJECTED = "이미 지난 예약은 취소할 수 없습니다.";

    private final Long id;
    private final Member reserver;
    private final Slot slot;

    public Reservation(
            Long id,
            Member reserver,
            Slot slot
    ) {
        this.id = id;
        this.reserver = Objects.requireNonNull(reserver);
        this.slot = Objects.requireNonNull(slot);
    }

    // TODO: 테스트에서 사용하는 메서드
    public Reservation(
            Long id,
            String name,
            LocalDate date,
            ReservationTime time,
            Theme theme
    ) {
        this(id, new Member(name), new Slot(date, time, theme));
    }

    public Reservation(
            String name,
            LocalDate date,
            ReservationTime time,
            Theme theme
    ) {
        this(null, new Member(name), new Slot(date, time, theme));
    }

    public static Reservation createWith(
            Member reserver,
            Slot slot,
            LocalDateTime now
    ) {
        validateCreatable(slot, now);
        return new Reservation(null, reserver, slot);
    }

    private static void validateCreatable(Slot slot, LocalDateTime now) {
        if (slot.isPast(now)) {
            throw new BusinessRuleViolationException(PAST_RESERVATION_CREATE_REJECTED);
        }
    }

    public Reservation updateWith(
            Member requester,
            Slot targetSlot,
            LocalDateTime now
    ) {
        validateOwner(requester);
        validatePast(now, EXPIRED_RESERVATION_UPDATE_REJECTED);
        validateTargetNotPast(targetSlot, now);

        return new Reservation(
                this.id,
                this.reserver,
                targetSlot
        );
    }

    // TODO: 테스트에서 사용하는 메서드
    public void cancelBy(String name, LocalDateTime now) {
        cancelBy(new Member(name), now);
    }

    public void cancelBy(Member member, LocalDateTime now) {
        validateOwner(member);
        validatePast(now, PAST_RESERVATION_CANCEL_REJECTED);
    }

    // TODO: 테스트에서 사용하는 메서드
    public boolean isOwnedBy(Member other) {
        return reserver.equals(other);
    }

    public boolean isPast(LocalDateTime now) {
        return slot.isPast(now);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return reserver.name();
    }

    public Member getReserver() {
        return reserver;
    }

    public Slot getSlot() {
        return slot;
    }

    public LocalDate getDate() {
        return slot.date();
    }

    public ReservationTime getTime() {
        return slot.time();
    }

    public Theme getTheme() {
        return slot.theme();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Reservation that = (Reservation) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    private void validateOwner(Member member) {
        if (!reserver.equals(member)) {
            throw new ForbiddenException(NOT_OWNER);
        }
    }

    private void validatePast(LocalDateTime now, String message) {
        if (isPast(now)) {
            throw new BusinessRuleViolationException(message);
        }
    }

    private void validateTargetNotPast(Slot slot, LocalDateTime now) {
        if (slot.isPast(now)) {
            throw new BusinessRuleViolationException(PAST_RESERVATION_UPDATE_REJECTED);
        }
    }
}
