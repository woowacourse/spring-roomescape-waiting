package roomescape.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import roomescape.domain.reservation.Reservation;
import roomescape.dto.reservation.AvailableReservationTimeSearch;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByMember_Id(final Long memberId);

    @Query("SELECT r.time.id FROM Reservation r WHERE r.date = :#{#condition.date} AND r.theme.id = :#{#condition.themeId}")
    List<Long> findTimeIds(@Param("condition") final AvailableReservationTimeSearch condition);

    List<Reservation> findByTheme_IdAndMember_IdAndDateBetween(final Long themeId, final Long memberId, final LocalDate dateFrom, final LocalDate dateTo);

    int countByTime_Id(final Long timeId);

    boolean existsByDateAndTime_IdAndTheme_Id(final LocalDate date, final Long timeId, final Long themeId);

    boolean existsById(final Long id);
}
