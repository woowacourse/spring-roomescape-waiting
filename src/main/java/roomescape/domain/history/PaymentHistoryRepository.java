package roomescape.domain.history;

import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import org.springframework.stereotype.Repository;

/**
 * 학습용 인메모리 결제 내역 저장소. 최근 것이 위로 오도록 앞에 쌓는다.
 */
@Repository
public class PaymentHistoryRepository {

    private final Deque<PaymentHistory> store = new ConcurrentLinkedDeque<>();

    public void save(PaymentHistory history) {
        store.addFirst(history);
    }

    /**
     * 최근순(newest-first) 전체 내역.
     */
    public List<PaymentHistory> findAll() {
        return new ArrayList<>(store);
    }

}
