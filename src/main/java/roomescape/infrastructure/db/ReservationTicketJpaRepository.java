package roomescape.infrastructure.db;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.model.ReservationTicket;
import roomescape.model.ReservationTime;
import roomescape.model.Theme;

public interface ReservationTicketJpaRepository extends JpaRepository<ReservationTicket, Long> {

    Optional<ReservationTicket> findByReservation_DateAndReservation_ReservationTime(LocalDate date,
                                                                                     ReservationTime time);

    List<ReservationTicket> findByReservation_ThemeIdAndReservation_MemberIdAndReservation_DateBetween(
            Long themeId,
            Long memberId,
            LocalDate dateAfter,
            LocalDate dateBefore);

    List<ReservationTicket> findByReservation_ThemeIdAndReservation_Date(final Long themeId, final LocalDate date);

    List<ReservationTicket> findByReservation_MemberId(Long id);

    List<ReservationTicket> findByReservation_ThemeId(Long id);

    List<ReservationTicket> findByReservation_ReservationTimeId(Long id);

    Optional<ReservationTicket> findByReservation_ThemeAndReservation_ReservationTimeAndReservation_Date(
            Theme theme,
            ReservationTime reservationTime,
            LocalDate date
    );
}
