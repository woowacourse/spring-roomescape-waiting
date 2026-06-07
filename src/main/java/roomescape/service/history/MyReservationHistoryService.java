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
                parseStatus(row.status()),
                row.name(),
                row.date(),
                row.theme(),
                row.time(),
                row.sequence()
        );
    }

    private ReservationHistoryStatus parseStatus(final String status) {
        try {
            return ReservationHistoryStatus.valueOf(status);
        } catch (IllegalArgumentException exception) {
            // 사용자 입력이 아니라 저장된 데이터가 손상된 상황이므로 400이 아닌 서버 오류(500)로 다룬다.
            throw new IllegalStateException("알 수 없는 예약 내역 상태입니다: " + status, exception);
        }
    }
}
