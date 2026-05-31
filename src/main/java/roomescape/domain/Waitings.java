package roomescape.domain;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import roomescape.common.DomainAssert;
import roomescape.common.exception.BusinessRuleViolationException;
import roomescape.common.exception.DuplicateEntityException;

public class Waitings {
    private static final int MAX_WAITING_COUNT = 5;

    private final Slot slot;
    private final List<Waiting> waitings;

    public Waitings(Slot slot, List<Waiting> waitings) {
        DomainAssert.notNull(slot, "슬롯은 비어 있을 수 없습니다.");
        DomainAssert.notNull(waitings, "대기 목록은 비어 있을 수 없습니다.");
        validateSameSlot(slot, waitings);
        this.slot = slot;
        this.waitings = waitings.stream()
                .sorted(Comparator.comparing(Waiting::getId, Comparator.nullsLast(Long::compareTo)))
                .toList();
    }

    public Waiting create(Member member, Reservation reservation) {
        validateSameSlot(reservation);
        validateCanCreate(member);
        return Waiting.create(member, reservation);
    }

    public Long nextRank() {
        return (long) waitings.size() + 1;
    }

    public Waiting assignRank(Waiting waiting) {
        for (int i = 0; i < waitings.size(); i++) {
            if (Objects.equals(waitings.get(i).getId(), waiting.getId())) {
                return waiting.withRank((long) i + 1);
            }
        }
        return waiting;
    }

    public List<Waiting> assignRanks() {
        return waitings.stream()
                .map(this::assignRank)
                .toList();
    }

    public List<Waiting> assignRanksOfMember(Long memberId) {
        return assignRanks().stream()
                .filter(waiting -> Objects.equals(waiting.getMember().getId(), memberId))
                .toList();
    }

    private void validateCanCreate(Member member) {
        if (waitings.stream().anyMatch(waiting -> waiting.isSameMember(member))) {
            throw new DuplicateEntityException("이미 대기 신청한 슬롯입니다.");
        }
        if (waitings.size() >= MAX_WAITING_COUNT) {
            throw new BusinessRuleViolationException("대기는 최대 5명까지만 가능합니다.");
        }
    }

    private void validateSameSlot(Reservation reservation) {
        if (!Objects.equals(reservation.getSlot(), slot)) {
            throw new BusinessRuleViolationException("같은 슬롯의 예약에만 대기를 생성할 수 있습니다.");
        }
    }

    private void validateSameSlot(Slot slot, List<Waiting> waitings) {
        boolean hasDifferentSlot = waitings.stream()
                .map(Waiting::getSlot)
                .anyMatch(waitingSlot -> !Objects.equals(waitingSlot, slot));
        if (hasDifferentSlot) {
            throw new BusinessRuleViolationException("같은 슬롯의 대기만 묶을 수 있습니다.");
        }
    }
}
