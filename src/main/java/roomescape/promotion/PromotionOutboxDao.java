package roomescape.promotion;

import java.util.List;
import roomescape.promotion.OutboxStatus;
import roomescape.promotion.PromotionTask;

public interface PromotionOutboxDao {

    PromotionTask insert(PromotionTask task);

    List<PromotionTask> findByStatus(OutboxStatus status);

    void markDone(Long id);
}
