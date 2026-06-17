package roomescape.waiting;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.IntStream;

import roomescape.common.DomainAssert;
import roomescape.common.exception.BusinessRuleViolationException;
import roomescape.common.exception.DuplicateEntityException;
import roomescape.domain.member.Member;
import roomescape.reservation.Reservation;
import roomescape.common.vo.Slot;

public class Waitings {
    private static final int MAX_WAITING_COUNT = 5;

    private final Slot slot;
    private final List<Waiting> waitings;

    public Waitings(Slot slot, List<Waiting> waitings) {
        DomainAssert.notNull(slot, "슬롯은 비어 있을 수 없습니다.");
        DomainAssert.notNull(waitings, "대기 목록은 비어 있을 수 없습니다.");
        validateAllBelongToSlot(slot, waitings);
        this.slot = slot;
        this.waitings = assignRanks(waitings);
    }

    public Waiting enqueue(Member member, Reservation reservation, LocalDateTime now) {
        validateReservationMatchesSlot(reservation);
        validateNotReservationOwner(member, reservation);
        validateCanCreate(member);
        Waiting created = Waiting.create(member, slot, now);
        return created.withRank((long) waitings.size() + 1);
    }

    private void validateNotReservationOwner(Member member, Reservation reservation) {
        if (reservation.isSameMember(member)) {
            throw new BusinessRuleViolationException("동일한 사용자의 예약이 존재합니다.");
        }
    }

    public Optional<Waiting> peekFirst() {
        return waitings.stream().findFirst();
    }

    public boolean isEmpty() {
        return waitings.isEmpty();
    }

    public List<Waiting> getAll() {
        return waitings;
    }

    public List<Waiting> ofMember(Long memberId) {
        return waitings.stream()
                .filter(waiting -> Objects.equals(waiting.getMemberId(), memberId))
                .toList();
    }

    private List<Waiting> assignRanks(List<Waiting> waitings) {
        List<Waiting> sorted = waitings.stream()
                .sorted(Comparator.comparing(Waiting::getId, Comparator.nullsLast(Long::compareTo)))
                .toList();
        return IntStream.range(0, sorted.size())
                .mapToObj(i -> sorted.get(i).withRank((long) i + 1))
                .toList();
    }

    private void validateCanCreate(Member member) {
        if (waitings.stream()
                .anyMatch(waiting -> waiting.isSameMember(member))) {
            throw new DuplicateEntityException("이미 대기 신청한 슬롯입니다.");
        }
        if (waitings.size() >= MAX_WAITING_COUNT) {
            throw new BusinessRuleViolationException("대기는 최대 5명까지만 가능합니다.");
        }
    }

    private void validateReservationMatchesSlot(Reservation reservation) {
        if (!reservation.isOnSlot(slot)) {
            throw new BusinessRuleViolationException("같은 슬롯의 예약에만 대기를 생성할 수 있습니다.");
        }
    }

    private void validateAllBelongToSlot(Slot slot, List<Waiting> waitings) {
        boolean hasDifferentSlot = waitings.stream()
                .anyMatch(waiting -> !waiting.isOnSlot(slot));
        if (hasDifferentSlot) {
            throw new BusinessRuleViolationException("같은 슬롯의 대기만 묶을 수 있습니다.");
        }
    }
}
