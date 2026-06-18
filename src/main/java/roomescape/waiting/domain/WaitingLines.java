package roomescape.waiting.domain;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class WaitingLines {

    private final Map<Long, WaitingLine> linesBySlotId;

    private WaitingLines(List<Waiting> waitings) {
        this.linesBySlotId = waitings.stream()
                .collect(Collectors.groupingBy(
                        Waiting::getSlotId,
                        Collectors.collectingAndThen(Collectors.toList(), WaitingLine::of)
                ));
    }

    public static WaitingLines of(List<Waiting> waitings) {
        return new WaitingLines(waitings);
    }

    public long orderOf(Waiting waiting) {
        return orderOf(waiting.getSlotId(), waiting.getId());
    }

    public long orderOf(Long slotId, Long waitingId) {
        WaitingLine waitingLine = linesBySlotId.get(slotId);
        if (waitingLine == null) {
            throw new IllegalArgumentException("대기열에 존재하지 않는 대기입니다.");
        }
        return waitingLine.orderOf(waitingId);
    }
}
