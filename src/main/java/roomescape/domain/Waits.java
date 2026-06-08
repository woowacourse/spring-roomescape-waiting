package roomescape.domain;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import roomescape.exception.custom.AlreadyWaitingException;
import roomescape.exception.custom.WaitIsFullException;

public class Waits {

    public static final int MAX_WAITING_COUNT = 3;

    private final List<Wait> waits;

    public Waits(List<Wait> waits) {
        this.waits = waits;
    }

    public void validateCreate(String name) {
        validateNotDuplicated(name);
        validateWaitIsNotFull();
    }

    public boolean isFullWait() {
        return waits.size() >= MAX_WAITING_COUNT;
    }

    public boolean isEmptyWait() {
        return waits.isEmpty();
    }

    public Wait firstWait() {
        Wait firstWait = waits.getFirst();
        return new Wait(firstWait.getId(), firstWait.getCreatedAt(), firstWait.getName(), firstWait.getSlot());
    }

    public Map<Wait, Long> waitWithOrder() {
        return waits.stream()
                .collect(Collectors.toMap(wait -> wait, this::calculateOrder,
                        (oldValue, newValue) -> oldValue,
                        LinkedHashMap::new
                ));
    }

    public Map<Wait, Long> waitWithOrderByName(String name) {
        return waitWithOrder().entrySet().stream()
                .filter(entry -> entry.getKey().isSameUser(name)) // 1. 이름 필터링 (완벽함)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,                 // 2. 새 Map의 Key = 원래 Wait 객체
                        Map.Entry::getValue,               // 3. 새 Map의 Value = 원래 순번(Long)
                        (oldValue, newValue) -> oldValue,   // 4. 중복 키 충돌 해결 규칙
                        LinkedHashMap::new                 // 5. 순서 보장을 위해 LinkedHashMap 사용
                ));
    }

    public Long calculateOrder(Wait myWait) {
        return waits.stream()
                .filter(wait -> wait.isFastCreatedAt(myWait.getCreatedAt()))
                .count() + 1;
    }

    public List<Wait> getWaits() {
        return List.copyOf(waits);
    }

    public Long size() {
        return (long) waits.size();
    }

    private void validateNotDuplicated(String name) {
        boolean isDuplicated = waits.stream()
                .anyMatch(wait -> wait.isSameUser(name));
        if (isDuplicated) {
            throw new AlreadyWaitingException();
        }
    }

    private void validateWaitIsNotFull() {
        if (isFullWait()) {
            throw new WaitIsFullException();
        }
    }
}
