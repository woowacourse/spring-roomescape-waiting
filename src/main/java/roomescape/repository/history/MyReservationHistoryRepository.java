package roomescape.repository.history;

import java.util.List;
import roomescape.repository.history.dto.MyReservationHistoryRow;

public interface MyReservationHistoryRepository {
    List<MyReservationHistoryRow> findByUserName(String name);
}
