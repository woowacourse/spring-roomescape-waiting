package roomescape.history.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.history.MyHistory;
import roomescape.history.repository.MyHistoryRepository;

@Service
public class MyHistoryService {

    private final MyHistoryRepository myHistoryRepository;

    public MyHistoryService(final MyHistoryRepository myHistoryRepository) {
        this.myHistoryRepository = myHistoryRepository;
    }

    @Transactional
    public List<MyHistory> getAllByName(final String name) {
        return myHistoryRepository.findByUserName(name);
    }
}
