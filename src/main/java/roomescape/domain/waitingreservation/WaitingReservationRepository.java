package roomescape.domain.waitingreservation;

public interface WaitingReservationRepository {

    WaitingReservation save(WaitingReservation waitingReservation);

    boolean existsByNameAndDateIdAndTimeIdAndThemeId(String name, long dateId, long timeId, long themeId);
}
