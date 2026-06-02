package roomescape.dao;

import java.util.List;
import roomescape.domain.Slot;
import roomescape.domain.Waiting;
import roomescape.domain.Waitings;

public interface WaitingDao extends CommonDao<Waiting> {

    Waitings findQueueBySlot(Slot slot);

    Waitings findQueueBySlotForUpdate(Slot slot);

    List<Waitings> findAllQueues();

    List<Waitings> findQueuesContainingMember(Long memberId);

    List<Waitings> findQueuesByStoreId(Long storeId);
}
