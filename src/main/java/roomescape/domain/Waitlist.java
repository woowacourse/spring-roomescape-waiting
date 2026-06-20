package roomescape.domain;

import static roomescape.domain.exception.DomainErrorCode.UNAUTHORIZED_RESERVATION;

import java.time.LocalDate;
import java.time.LocalDateTime;
import roomescape.domain.exception.RoomEscapeException;

public class Waitlist {

    private final Long id;
    private final Member member;
    private final LocalDateTime createdAt;
    private final Slot slot;

    public Waitlist(Long id, Member member, Slot slot, LocalDateTime createdAt) {
        this.id = id;
        this.member = member;
        this.createdAt = createdAt;
        this.slot = slot;
    }

    public Waitlist(Member member, Slot slot, LocalDateTime createdAt) {
        this(null, member, slot, createdAt);
    }

    public void verifyCancelableBy(String name) {
        verifyReservedBy(name, "본인의 대기 예약만 취소할 수 있습니다.");
    }

    private void verifyReservedBy(String other, String message) {
        if (!this.member.getName().equals(other)) {
            throw new RoomEscapeException(UNAUTHORIZED_RESERVATION, message);
        }
    }

    public Long getId() {
        return id;
    }

    public Member getMember() {
        return member;
    }

    public String getName() {
        return member.getName();
    }

    public Slot getSlot() {
        return slot;
    }

    public LocalDate getDate() {
        return slot.getDate();
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public ReservationTime getTime() {
        return slot.getTime();
    }

    public Theme getTheme() {
        return slot.getTheme();
    }
}
