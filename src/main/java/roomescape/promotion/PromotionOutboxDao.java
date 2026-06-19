package roomescape.promotion;

import java.util.List;

public interface PromotionOutboxDao {

    PromotionTask insert(PromotionTask task);

    List<PromotionTask> findByStatus(OutboxStatus status);

    void markDone(Long id);
}
