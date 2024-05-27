package roomescape.infrastructure;

import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import org.springframework.stereotype.Component;
import roomescape.domain.member.Member;
import roomescape.domain.concurrency.AtomicQueue;
import roomescape.domain.reservation.slot.ReservationSlot;

@Component
public class ReservationWaitingQueue implements AtomicQueue<ReservationSlot, Member> {

    private final Map<ReservationSlot, Queue<Member>> waitingMembers;
    private final Map<ReservationSlot, AtomicBoolean> locks;

    public ReservationWaitingQueue() {
        this.waitingMembers = new ConcurrentHashMap<>();
        this.locks = new ConcurrentHashMap<>();
    }

    @Override
    public boolean ifFirstRunElseWait(Member member, ReservationSlot slot, BiConsumer<Member, ReservationSlot> callback) {
        add(member, slot);

        AtomicBoolean lock = getLock(slot);
        if (isFirst(member, slot)) {
            try {
                callback.accept(member, slot);
            } finally {
                lock.set(false);
            }
            return true;
        }

        sleepWhileLock(lock, 500L);
        reset(slot);
        return false;
    }

    @Override
    public void add(Member member, ReservationSlot slot) {
        waitingMembers.computeIfAbsent(slot, k -> new ConcurrentLinkedQueue<>())
                .add(member);
    }

    @Override
    public AtomicBoolean getLock(ReservationSlot slot) {
        return locks.computeIfAbsent(slot, k -> new AtomicBoolean(true));
    }

    @Override
    public boolean isFirst(Member member, ReservationSlot slot) {
        return Objects.equals(waitingMembers.get(slot).peek(), member);
    }

    @Override
    public void sleepWhileLock(AtomicBoolean lock, long time) {
        while (lock.get()) {
            try {
                Thread.sleep(time);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void reset(ReservationSlot slot) {
        waitingMembers.remove(slot);
        locks.remove(slot);
    }
}
