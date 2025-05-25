package roomescape.infrastructure.db;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.model.ReservationTicket;
import roomescape.model.ReservationTime;

public interface ReservationTicketJpaRepository extends JpaRepository<ReservationTicket, Long> {

    Optional<ReservationTicket> findByDateAndReservationTime(LocalDate date, ReservationTime time);

    List<ReservationTicket> findByThemeIdAndMemberIdAndDateBetween(
            Long themeId,
            Long memberId,
            LocalDate dateAfter,
            LocalDate dateBefore);

    List<ReservationTicket> findByThemeIdAndDate(final Long themeId, final LocalDate date);

    List<ReservationTicket> findByMemberId(Long id);

    List<ReservationTicket> findByThemeId(Long id);

    List<ReservationTicket> findByReservationTimeId(Long id);
}
