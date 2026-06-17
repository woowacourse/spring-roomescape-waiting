package roomescape.dao;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import roomescape.common.vo.Slot;
import roomescape.domain.waiting.Waiting;
import roomescape.domain.waiting.Waitings;

public interface WaitingDao extends CommonDao<Waiting> {

    Waitings findQueueBySlot(Slot slot);

    Waitings findQueueBySlotForUpdate(Slot slot);

    Optional<Waiting> findFirstBySlotKeyForUpdate(Long themeId, Long timeId, LocalDate date, Long storeId);

    List<Waitings> findAllQueues();

    List<Waitings> findQueuesContainingMember(Long memberId);

    List<Waitings> findQueuesByStoreId(Long storeId);
}
