package roomescape.waiting;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class WaitingLines {

    private final Map<Long, WaitingLine> linesBySlotId;

    public static WaitingLines of(List<Waiting> waitings) {
        return new WaitingLines(waitings);
    }

    private WaitingLines(List<Waiting> waitings) {
        this.linesBySlotId = waitings.stream()
                .collect(Collectors.groupingBy(
                        Waiting::getSlotId,
                        Collectors.collectingAndThen(Collectors.toList(), WaitingLine::of)
                ));
    }

    public long orderOf(Waiting waiting) {
        WaitingLine waitingLine = linesBySlotId.get(waiting.getSlotId());
        if (waitingLine == null) {
            throw new IllegalArgumentException("대기열에 존재하지 않는 대기입니다.");
        }
        return waitingLine.orderOf(waiting);
    }
}
