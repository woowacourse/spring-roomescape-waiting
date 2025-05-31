package roomescape.persistence.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import roomescape.model.ReservationSpec;
import roomescape.model.ReservationTicket;
import roomescape.persistence.vo.Period;

public interface ReservationTicketRepository {

    boolean isDuplicated(ReservationSpec reservationSpec);

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

    Optional<ReservationTicket> findForThemeAndReservationTimeOnDate(ReservationSpec reservationSpec);
}
