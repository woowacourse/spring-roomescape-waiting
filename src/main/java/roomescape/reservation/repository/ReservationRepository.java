package roomescape.reservation.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationStatus;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    @Override
    @EntityGraph(attributePaths = {"member", "theme", "time"})
    List<Reservation> findAll();

    @Override
    @EntityGraph(attributePaths = {"member", "theme", "time"})
    Optional<Reservation> findById(Long id);

    @Query("""
            select r.time.id from Reservation r 
            join fetch ReservationTime rt on r.time.id = rt.id  
            where r.date = :date and r.theme.id = :themeId
            """)
    List<Long> findTimeIdsByDateAndThemeId(LocalDate date, Long themeId);

    @Query("""
           select r from Reservation r
           join fetch ReservationTime rt on rt.id = r.time.id
           join fetch Theme t on t.id = r.theme.id
           join fetch Member m on m.id = r.member.id
           where m.id = :memberId and r.date >= :date
           order by r.date, rt.startAt, r.createdAt
            """)
    List<Reservation> findAllByMemberIdFromDateOrderByDateAscTimeStartAtAscCreatedAtAsc(Long memberId, LocalDate date);

    @EntityGraph(attributePaths = {"member", "theme", "time"})
    List<Reservation> findAllByThemeIdAndMemberIdAndDateBetweenOrderByDateAscTimeStartAtAscCreatedAtAsc(
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

    @Query("""
           select r from Reservation r
           join fetch ReservationTime rt on rt.id = r.time.id
           join fetch Theme t on t.id = r.theme.id
           join fetch Member m on m.id = r.member.id
           where r.status = :status and r.date >= :date
           order by r.date, rt.startAt, r.createdAt
            """)
    List<Reservation> findAllByStatusFromDate(ReservationStatus status, LocalDate date);

    @EntityGraph(attributePaths = {"time"})
    boolean existsByDateAndTimeStartAtAndStatus(LocalDate date, LocalTime startAt, ReservationStatus status);

    @Query("""
           select r.status from Reservation r
           join ReservationTime rt on r.time.id = rt.id
           where r.member.id = :memberId and r.date = :date and rt.startAt = :startAt
            """)
    List<ReservationStatus> findStatusesByMemberIdAndDateAndTimeStartAt(Long memberId, LocalDate date, LocalTime startAt);
}
