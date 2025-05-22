package roomescape.persistence.repository;

import java.time.LocalDate;
import java.util.List;
import roomescape.model.Reservation;
import roomescape.model.ReservationTime;
import roomescape.persistence.vo.Period;

public interface ReservationRepository {

    boolean isDuplicatedForDateAndReservationTime(LocalDate date, ReservationTime time);

    List<Reservation> findForThemeAndMemberInPeriod(
            Long themeId,
            Long memberId,
            Period period
    );

    List<Reservation> findForThemeOnDate(Long themeId, LocalDate date);

    List<Reservation> findForMember(Long memberId);

    Reservation save(Reservation reservation);

    List<Reservation> findAll();

    void deleteById(Long id);
}
