package roomescape.service.history;

import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.repository.history.MyReservationHistoryRepository;
import roomescape.repository.history.dto.MyReservationHistoryRow;

@Service
public class MyReservationHistoryService {

    private final MyReservationHistoryRepository myReservationHistoryRepository;

    public MyReservationHistoryService(final MyReservationHistoryRepository myReservationHistoryRepository) {
        this.myReservationHistoryRepository = myReservationHistoryRepository;
    }

    public List<MyReservationHistory> getAllByName(final String name) {
        return myReservationHistoryRepository.findByUserName(name).stream()
                .map(this::toHistory)
                .toList();
    }

    private MyReservationHistory toHistory(final MyReservationHistoryRow row) {
        return new MyReservationHistory(
                row.reservationId(),
                row.waitingId(),
                ReservationHistoryStatus.valueOf(row.status()),
                row.name(),
                row.date(),
                row.theme(),
                row.time(),
                row.sequence()
        );
    }
}
