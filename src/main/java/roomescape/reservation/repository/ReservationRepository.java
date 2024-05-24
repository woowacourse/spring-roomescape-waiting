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

    @Query("""
           select r from Reservation r
           join fetch ReservationTime rt on rt.id = r.reservationTime.id
           join fetch Theme t on t.id = r.theme.id
           join fetch Member m on m.id = r.member.id
           where m.id = :memberId and r.date >= :date
           order by r.date, rt.startAt, r.createdAt
            """)
    List<Reservation> findAllByMemberIdFromDateOrderByDateAscTimeAscCreatedAtAsc(Long memberId, LocalDate date);

    @EntityGraph(attributePaths = {"member", "theme", "reservationTime"})
    List<Reservation> findAllByThemeIdAndMemberIdAndDateBetweenOrderByDateAscReservationTimeAscCreatedAtAsc(
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
           join fetch ReservationTime rt on rt.id = r.reservationTime.id
           join fetch Theme t on t.id = r.theme.id
           join fetch Member m on m.id = r.member.id
           where r.reservationStatus = :reservationStatus and r.date >= :date
           order by r.date, rt.startAt, r.createdAt
            """)
    List<Reservation> findAllByReservationStatusFromDate(ReservationStatus reservationStatus, LocalDate date);

    @EntityGraph(attributePaths = {"reservationTime"})
    boolean existsByDateAndReservationTimeStartAtAndReservationStatus(LocalDate date, LocalTime startAt, ReservationStatus reservationStatus);

    @Query("""
           select r.reservationStatus from Reservation r
           join ReservationTime rt on r.reservationTime.id = rt.id
           where r.member.id = :memberId and r.date = :date and rt.startAt = :startAt
            """)
    List<ReservationStatus> findStatusesByMemberIdAndDateAndReservationTimeStartAt(Long memberId, LocalDate date, LocalTime startAt);
}
