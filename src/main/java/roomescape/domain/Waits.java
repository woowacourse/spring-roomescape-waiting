package roomescape.domain;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import roomescape.exception.custom.AlreadyWaitingException;
import roomescape.exception.custom.WaitIsFullException;

public class Waits {

    public static final int MAX_WAITING_COUNT = 3;

    private final List<Wait> waits;

    public Waits(List<Wait> waits) {
        this.waits = waits;
    }

    public void validateCreate(Member member, Slot slot) {
        validateNotDuplicated(member, slot);
        validateWaitsIsNotFull(slot);
    }

    public boolean isFullWaitsBySlot(Slot slot) {
        List<Wait> waitsBySlot = waits.stream()
                .filter(wait -> wait.isSameSlot(slot))
                .toList();
        return waitsBySlot.size() >= MAX_WAITING_COUNT;
    }

    public boolean isEmptyWaitsBySlot(Slot slot) {
        List<Wait> waitsBySlot = waits.stream()
                .filter(wait -> wait.isSameSlot(slot))
                .toList();
        return waitsBySlot.isEmpty();
    }

    public Wait firstWaitBySlot(Slot slot) {
        return waits.stream()
                .filter(wait -> wait.isSameSlot(slot))
                .min(Comparator.comparing(Wait::getCreatedAt))
                .orElse(null);
    }

    public Map<Wait, Long> waitsWithOrder() {
        return waits.stream()
                .collect(Collectors.toMap(wait -> wait, this::calculateOrder,
                        (oldValue, newValue) -> oldValue,
                        LinkedHashMap::new
                ));
    }

    public Map<Wait, Long> waitsWithOrderByName(String name) {
        return waitsWithOrder().entrySet().stream()
                .filter(entry -> entry.getKey().isSameUser(name))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue,
                        LinkedHashMap::new
                ));
    }

    public Long calculateOrder(Wait myWait) {
        return waits.stream()
                .filter(wait -> wait.isSameSlot(myWait.getSlot()))
                .filter(wait -> wait.isFastCreatedAt(myWait.getCreatedAt()))
                .count() + 1;
    }

    private void validateNotDuplicated(Member member, Slot slot) {
        boolean isDuplicated = waits.stream()
                .anyMatch(wait -> wait.isSameUser(member) && wait.isSameSlot(slot));
        if (isDuplicated) {
            throw new AlreadyWaitingException();
        }
    }

    private void validateWaitsIsNotFull(Slot slot) {
        if (isFullWaitsBySlot(slot)) {
            throw new WaitIsFullException();
        }
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        Waits waits1 = (Waits) object;
        return Objects.equals(waits, waits1.waits);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(waits);
    }
}
