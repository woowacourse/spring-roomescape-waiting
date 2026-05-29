package roomescape.history.repository;

import java.util.List;
import roomescape.history.MyHistory;

public interface MyHistoryRepository {
    List<MyHistory> findByUserName(String name);
}
