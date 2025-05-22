package roomescape.persistence;

import java.time.LocalDate;
import java.util.List;
import roomescape.model.Reservation;
import roomescape.model.ReservationTime;

public interface ReservationRepository {

    Optional<Reservation> findDuplicatedReservationByDateAndTime(LocalDate date, ReservationTime time);

    List<Reservation> findForThemeAndMemberInPeriod(
            Long themeId,
            Long memberId,
            LocalDate startDate,
            LocalDate endDate
    );

    List<Reservation> findForThemeOnDate(Long themeId, LocalDate date);

    List<Reservation> findForMember(Long memberId);

    List<Reservation> findForTheme(Long themeId);

    List<Reservation> findForReservationTime(Long reservationTimeId);

    Reservation save(Reservation reservation);

    List<Reservation> findAll();

    void deleteById(Long id);
}
