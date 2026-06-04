package roomescape.dao;

import java.util.List;
import roomescape.domain.OutboxStatus;
import roomescape.domain.PromotionTask;

public interface PromotionOutboxDao {

    PromotionTask insert(PromotionTask task);

    List<PromotionTask> findByStatus(OutboxStatus status);

    void markDone(Long id);
}
