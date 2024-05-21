package roomescape.reservation.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.Theme;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    @Query("select r.reservationTime.id from Reservation r where r.date = :date and r.theme.id = :themeId")
    List<Long> findTimeIdsByDateAndThemeId(LocalDate date, Long themeId);

    List<Reservation> findAllByMemberId(Long memberId);

    List<Reservation> findAllByThemeIdAndMemberIdAndDateBetween(
            Long themeId,
            Long memberId,
            LocalDate dateFrom,
            LocalDate dateTo
    );

    boolean existsByDateAndReservationTimeStartAtAndTheme(LocalDate date, LocalTime startAt, Theme theme);
}
