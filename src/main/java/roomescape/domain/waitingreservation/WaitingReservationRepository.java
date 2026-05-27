package roomescape.domain.waitingreservation;

import java.util.List;
import java.util.Optional;
import roomescape.domain.waitingreservation.dto.WaitingReservationWithRank;

public interface WaitingReservationRepository {

    WaitingReservation save(WaitingReservation waitingReservation);

    boolean existsByNameAndDateIdAndTimeIdAndThemeId(String name, long dateId, long timeId, long themeId);

    Optional<WaitingReservation> findOldest();

    List<WaitingReservationWithRank> findAllByNameWithRank(String name);
}
