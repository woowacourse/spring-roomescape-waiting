package roomescape.domain.repository;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.domain.member.Member;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationSlot;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.Theme;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    @Query("select r from Reservation r where r.reservationSlot.date = :date and r.reservationSlot.theme = :theme and r.reservationSlot.time = :time")
    Optional<Reservation> findByDateAndThemeAndTime(LocalDate date, Theme theme, ReservationTime time);

//    @EntityGraph(attributePaths = {"time", "theme"})
//    List<Reservation> findByMemberAndDateGreaterThanEqual(Member member, LocalDate date, Sort sort);

    @Query("select r from Reservation r where r.reservationSlot.date = :date and r.reservationSlot.theme = :theme")
    List<Reservation> findByDateAndTheme(LocalDate date, Theme theme);

    @Query("select r from Reservation r where r.reservationSlot.date between :startDate and :endDate")
    List<Reservation> findAllByDateBetween(LocalDate startDate, LocalDate endDate);

    @Query("""
            select r
            from Reservation r
            join fetch r.reservationSlot.theme
            join fetch r.reservationSlot.date
            join fetch r.member
            where (:startDate is null or r.reservationSlot.date >= :startDate)
                and (:endDate is null or r.reservationSlot.date <= :endDate)
                and (:themeId is null or r.reservationSlot.theme.id = :themeId)
                and (:memberId is null or r.member.id = :memberId)""")
    List<Reservation> findByConditions(
            Optional<LocalDate> startDate,
            Optional<LocalDate> endDate,
            Long themeId,
            Long memberId
    );

    List<Reservation> findByMember(Member member);

    boolean existsByMemberAndReservationSlot(Member member, ReservationSlot slot);

    @Query("select exists(select 1 from Reservation r where r.reservationSlot.time=:time)")
    boolean existsByTime(ReservationTime time);

    @Query("select exists(select 1 from Reservation r where r.reservationSlot.theme=:theme and r.reservationSlot.date =:date and r.reservationSlot.time = :time)")
    boolean existsByDateAndTimeAndTheme(LocalDate date, ReservationTime time, Theme theme);

    @Query("select exists(select 1 from Reservation r where r.reservationSlot.theme=:theme)")
    boolean existsByTheme(Theme theme);
}
