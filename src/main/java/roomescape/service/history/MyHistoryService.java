package roomescape.service.history;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import roomescape.controller.history.dto.HistoryResponse;
import roomescape.controller.reservationtime.dto.ReservationTimeResponse;
import roomescape.controller.theme.dto.ThemeResponse;
import roomescape.domain.history.ReservationHistoryStatus;
import roomescape.domain.reservationwaiting.ReservationWaitingLine;
import roomescape.repository.history.MyHistory;
import roomescape.repository.history.MyHistoryRepository;
import roomescape.repository.history.MyWaitingOrder;

@Service
public class MyHistoryService {

    private final MyHistoryRepository myHistoryRepository;

    public MyHistoryService(final MyHistoryRepository myHistoryRepository) {
        this.myHistoryRepository = myHistoryRepository;
    }

    public List<HistoryResponse> getAllByName(final String name) {
        List<MyHistory> histories = myHistoryRepository.findByUserName(name);
        Map<Long, Integer> waitingSequences = findWaitingSequences(histories);

        return histories.stream()
                .map(history -> toResponse(history, waitingSequences))
                .toList();
    }

    private Map<Long, Integer> findWaitingSequences(final List<MyHistory> histories) {
        List<Long> waitingReservationIds = histories.stream()
                .filter(history -> history.status() == ReservationHistoryStatus.WAITING)
                .map(MyHistory::reservationId)
                .distinct()
                .toList();

        Map<Long, List<MyWaitingOrder>> ordersByReservationId = myHistoryRepository
                .findWaitingOrdersByReservationIds(waitingReservationIds)
                .stream()
                .collect(Collectors.groupingBy(MyWaitingOrder::reservationId));

        return ordersByReservationId.entrySet().stream()
                .flatMap(entry -> toSequenceEntries(entry.getValue()).entrySet().stream())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private Map<Long, Integer> toSequenceEntries(final List<MyWaitingOrder> orders) {
        ReservationWaitingLine waitingLine = new ReservationWaitingLine(orders.stream()
                .map(order -> new ReservationWaitingLine.ReservationWaitingOrder(
                        order.waitingId(),
                        order.requestedAt()
                ))
                .toList());

        return orders.stream()
                .collect(Collectors.toMap(
                        MyWaitingOrder::waitingId,
                        order -> waitingLine.sequenceOf(order.waitingId())
                ));
    }

    private HistoryResponse toResponse(final MyHistory history, final Map<Long, Integer> waitingSequences) {
        return new HistoryResponse(
                history.reservationId(),
                history.waitingId(),
                history.status(),
                history.name(),
                history.date(),
                ThemeResponse.from(history.theme()),
                ReservationTimeResponse.from(history.time()),
                resolveSequence(history, waitingSequences)
        );
    }

    private Integer resolveSequence(final MyHistory history, final Map<Long, Integer> waitingSequences) {
        if (history.status() == ReservationHistoryStatus.RESERVATION) {
            return 0;
        }

        return waitingSequences.get(history.waitingId());
    }
}
