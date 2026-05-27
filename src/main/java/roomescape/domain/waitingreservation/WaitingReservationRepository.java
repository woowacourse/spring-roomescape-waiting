package roomescape.domain.waitingreservation;

import java.util.Optional;

public interface WaitingReservationRepository {

    WaitingReservation save(WaitingReservation waitingReservation);

    boolean existsByNameAndDateIdAndTimeIdAndThemeId(String name, long dateId, long timeId, long themeId);

    Optional<WaitingReservation> findOldest();
}
