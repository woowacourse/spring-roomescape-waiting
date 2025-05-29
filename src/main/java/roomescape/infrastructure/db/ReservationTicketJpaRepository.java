package roomescape.infrastructure.db;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.model.ReservationTicket;
import roomescape.model.ReservationTime;
import roomescape.model.Theme;

public interface ReservationTicketJpaRepository extends JpaRepository<ReservationTicket, Long> {

    Optional<ReservationTicket> findByReservationSpec_DateAndReservationSpec_ReservationTimeAndReservationSpec_Theme(
            LocalDate date,
            ReservationTime time,
            Theme theme);

    List<ReservationTicket> findByReservationSpec_ThemeIdAndReservationSpec_MemberIdAndReservationSpec_DateBetween(
            Long themeId,
            Long memberId,
            LocalDate dateAfter,
            LocalDate dateBefore);

    List<ReservationTicket> findByReservationSpec_ThemeIdAndReservationSpec_Date(final Long themeId,
                                                                                 final LocalDate date);

    List<ReservationTicket> findByReservationSpec_MemberId(Long id);

    List<ReservationTicket> findByReservationSpec_ThemeId(Long id);

    List<ReservationTicket> findByReservationSpec_ReservationTimeId(Long id);

    Optional<ReservationTicket> findByReservationSpec_ThemeAndReservationSpec_ReservationTimeAndReservationSpec_Date(
            Theme theme,
            ReservationTime reservationTime,
            LocalDate date
    );
}
