package roomescape.persistence.dao;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import roomescape.business.domain.PlayTime;

public interface PlayTimeDao {

    PlayTime insert(PlayTime playTime);

    List<PlayTime> findAll();

    Optional<PlayTime> findById(Long id);

    boolean deleteById(Long id);

    boolean existsById(Long timeId);

    boolean existsByStartAt(LocalTime startAt);
}
