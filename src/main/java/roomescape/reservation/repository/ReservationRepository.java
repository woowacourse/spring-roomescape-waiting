package roomescape.reservation.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.Status;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    @Override
    @EntityGraph(attributePaths = {"member", "theme", "reservationTime"})
    List<Reservation> findAll();

    @Override
    @EntityGraph(attributePaths = {"member", "theme", "reservationTime"})
    Optional<Reservation> findById(Long id);

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

    @Query(value = """
            select r
            from Reservation r
            join fetch Theme t
            on t.id = r.theme.id
            where r.date >= :dateFrom
               """)
    List<Reservation> findReservationsOfLastWeek(LocalDate dateFrom);

    @EntityGraph(attributePaths = {"member", "theme", "reservationTime"})
    List<Reservation> findAllByStatus(Status status);

    @EntityGraph(attributePaths = {"reservationTime"})
    boolean existsByDateAndReservationTimeStartAtAndStatus(LocalDate date, LocalTime startAt, Status status);

    @Query("""
           select r.status from Reservation r
           join ReservationTime rt on r.reservationTime.id = rt.id
           where r.member.id = :memberId and r.date = :date and rt.startAt = :startAt
            """)
    List<Status> findStatusesByMemberIdAndDateAndReservationTimeStartAt(Long memberId, LocalDate date, LocalTime startAt);
}
