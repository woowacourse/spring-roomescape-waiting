package roomescape.dao;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import roomescape.domain.Waiting;

public interface WaitingDao extends CommonDao<Waiting> {

    Optional<Waiting> findFirst(LocalDate date, Long timeId, Long themeId, Long storeId);

    List<Waiting> findAllByMemberId(Long memberId);

    List<Waiting> findAllByStoreId(Long storeId);
}
