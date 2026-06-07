package roomescape.dao;

import java.util.List;
import roomescape.domain.promotion.OutboxStatus;
import roomescape.domain.promotion.PromotionTask;

public interface PromotionOutboxDao {

    PromotionTask insert(PromotionTask task);

    List<PromotionTask> findByStatus(OutboxStatus status);

    void markDone(Long id);
}
