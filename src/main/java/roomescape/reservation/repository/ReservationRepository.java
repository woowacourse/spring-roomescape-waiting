package roomescape.reservation.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationSlot;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.dto.ReservationWithRank;
import roomescape.theme.domain.Theme;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findAll();

    @Query("SELECT COUNT(r) > 0 FROM Reservation r WHERE r.slot.date = :date AND r.slot.time = :time AND r.slot.theme = :theme")
    boolean existsByDateAndTimeAndTheme(@Param("date") LocalDate date,
            @Param("time") ReservationTime time,
            @Param("theme") Theme theme);

    @Query("SELECT r FROM Reservation r WHERE r.slot.theme = :theme AND r.member = :member AND r.slot.date BETWEEN :dateFrom AND :dateTo")
    List<Reservation> findByThemeAndMemberAndDateBetween(@Param("theme") Theme theme,
            @Param("member") Member member,
            @Param("dateFrom") LocalDate dateFrom,
            @Param("dateTo") LocalDate dateTo);

    @Query("""
            SELECT r.slot.theme
            FROM Reservation r
            WHERE r.slot.date >= :dateFrom AND r.slot.date < :dateTo
            GROUP BY r.slot.theme
            ORDER BY COUNT(r) DESC
            """)
    List<Theme> findPopularThemesByReservationBetween(@Param("dateFrom") LocalDate dateFrom,
            @Param("dateTo") LocalDate dateTo,
            Pageable pageable);

    List<Reservation> findByMember(Member member);

    @Query("SELECT new roomescape.reservation.dto.ReservationWithRank("
            + "r, "
            + "(SELECT COUNT(r2) + 1 "
            + "FROM Reservation r2 "
            + "WHERE r2.slot = r.slot "
            + "AND r2.id < r.id "
            + "AND r2.status = :status)) "
            + "FROM Reservation r "
            + "WHERE r.status = :status "
            + "AND r.member = :member")
    List<ReservationWithRank> findReservationsWithRankByMemberAndStatus(@Param("member") Member member,
            @Param("status") ReservationStatus status);

    @Query("SELECT new roomescape.reservation.dto.ReservationWithRank("
            + "r, "
            + "1) "
            + "FROM Reservation r "
            + "WHERE r.status = :status "
            + "AND r.member = :member")
    List<ReservationWithRank> findReservationsByMemberAndStatus(@Param("member") Member member,
                                                                        @Param("status") ReservationStatus status);

    @Query("SELECT new roomescape.reservation.dto.ReservationWithRank("
            + "r, "
            + "(SELECT COUNT(r2) + 1 "
            + "FROM Reservation r2 "
            + "WHERE r2.slot = r.slot "
            + "AND r2.id < r.id "
            + "AND r2.status = :status)) "
            + "FROM Reservation r "
            + "WHERE r.status = :status")
    List<ReservationWithRank> findAllReservationsWithRankByStatus(@Param("status") ReservationStatus status);

    @Query("SELECT COUNT(r) > 0 FROM Reservation r WHERE r.slot = :slot AND r.status = :status")
    boolean existsReservationBySlotAndStatus(@Param("slot") ReservationSlot slot,
            @Param("status") ReservationStatus status);

    @Query("SELECT COUNT(r) > 0 FROM Reservation r WHERE r.slot = :slot AND r.member = :member AND r.status = :status")
    boolean existsReservationBySlotAndMemberAndStatus(ReservationSlot slot, Member member, ReservationStatus status);

    @Query("SELECT r FROM Reservation r WHERE r.slot = :slot AND r.status = :status ORDER BY r.id LIMIT 1")
    Optional<Reservation> findFirstReservationsBySlotAndStatus(@Param("slot") ReservationSlot slot,
            @Param("status") ReservationStatus status);

    @Query("SELECT r FROM Reservation r WHERE r.status = :status")
    List<Reservation> findAllReservations(@Param("status") ReservationStatus status);

    default boolean existsConfirmedReservationBySlot(ReservationSlot slot) {
        return existsReservationBySlotAndStatus(slot, ReservationStatus.CONFIRMED);
    }
}
