package roomescape.persistence.repository;

import java.time.LocalDate;
import java.util.List;
import roomescape.model.ReservationTicket;
import roomescape.model.ReservationTime;
import roomescape.persistence.vo.Period;

public interface ReservationRepository {

    boolean isDuplicatedForDateAndReservationTime(LocalDate date, ReservationTime time);

    List<ReservationTicket> findForThemeAndMemberInPeriod(
            Long themeId,
            Long memberId,
            Period period
    );

    List<ReservationTicket> findForThemeOnDate(Long themeId, LocalDate date);

    List<ReservationTicket> findForMember(Long memberId);

    ReservationTicket save(ReservationTicket reservationTicket);

    List<ReservationTicket> findAll();

    void deleteById(Long id);

    ReservationTicket findById(Long id);
}
