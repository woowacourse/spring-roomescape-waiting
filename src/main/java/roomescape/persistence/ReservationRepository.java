package roomescape.persistence;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import roomescape.model.Reservation;
import roomescape.model.ReservationTime;

public interface ReservationRepository {

    Optional<Reservation> findDuplicatedReservationByDateAndTime(LocalDate date, ReservationTime time);

    List<Reservation> findReservationsForThemeAndMemberInPeriod(
            Long themeId,
            Long memberId,
            LocalDate startDate,
            LocalDate endDate
    );

    List<Reservation> findReservationsForThemeOnDate(Long themeId, LocalDate date);

    List<Reservation> findReservationForMember(Long memberId);

    List<Reservation> findReservationForTheme(Long themeId);

    List<Reservation> findReservationForReservationTime(Long reservationTimeId);

    Reservation saveReservation(Reservation reservation);

    List<Reservation> findAllReservations();

    void deleteWithId(Long id);
}
