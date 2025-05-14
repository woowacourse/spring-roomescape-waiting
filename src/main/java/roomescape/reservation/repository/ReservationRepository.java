package roomescape.reservation.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import roomescape.reservation.domain.Reservation;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    @Query("select count(*) from Reservation")
    boolean existsSameDateTime(LocalDate date, Long timeId);

    boolean existsByReservationDatetime_ReservationTime_Id(Long timeId);

    boolean existsByTheme_Id(Long themeId);

    @Query("select count(*) from Reservation")
    int countReservationByThemeIdAndDuration(LocalDate from, LocalDate to, Long themeId);

    @Query("select count(*) from Reservation")
    List<Long> findReservedTimeIdsByDateAndTheme(LocalDate date, Long themeId);

    @Query("select count(*) from Reservation")
    List<Reservation> findFilteredReservations(Long themeId, Long memberId, LocalDate from, LocalDate to);
}
