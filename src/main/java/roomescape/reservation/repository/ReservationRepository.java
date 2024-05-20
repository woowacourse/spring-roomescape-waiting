package roomescape.reservation.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.reservation.domain.Reservation;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    @Override
    @EntityGraph(attributePaths = {"member", "theme", "reservationTime"})
    List<Reservation> findAll();

    @Query("""
             select r.reservationTime.id from Reservation r 
             join fetch ReservationTime rt on r.reservationTime.id = rt.id  
             where r.date = :date and r.theme.id = :themeId
             """)
    List<Long> findTimeIdsByDateAndThemeId(LocalDate date, Long themeId);

    @EntityGraph(attributePaths = {"member", "theme", "reservationTime"})
    List<Reservation> findAllByMemberId(Long memberId);

    @EntityGraph(attributePaths = {"member", "theme", "reservationTime"})
    List<Reservation> findAllByThemeIdAndMemberIdAndDateBetween(
            Long themeId,
            Long memberId,
            LocalDate dateFrom,
            LocalDate dateTo
    );

    boolean existsByDateAndReservationTimeStartAt(LocalDate date, LocalTime startAt);
}
