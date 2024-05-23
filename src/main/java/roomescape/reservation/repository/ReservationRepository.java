package roomescape.reservation.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.reservation.model.Reservation;
import roomescape.reservation.model.Slot;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    @Query("""
                SELECT r
                FROM Reservation r
                JOIN FETCH r.member m
                JOIN FETCH r.slot.reservationTime rt
                JOIN FETCH r.slot.theme t
            """)
    List<Reservation> findAll();

    @Query("""
                SELECT r
                FROM Reservation r
                JOIN FETCH r.member m
                JOIN FETCH r.slot.reservationTime rt
                JOIN FETCH r.slot.theme t
                WHERE m.id = :memberId
            """)
    List<Reservation> findAllByMemberId(Long memberId);

    List<Reservation> findAllBySlot_DateAndSlot_ThemeId(LocalDate date, Long themeId);

    @Query("""
                SELECT r
                FROM Reservation r
                JOIN FETCH r.member m
                JOIN FETCH r.slot.reservationTime rt
                JOIN FETCH r.slot.theme t
                WHERE t.id = :themeId
                  AND m.id = :memberId
                  AND r.slot.date BETWEEN :dateFrom AND :dateTo
            """)
    List<Reservation> findAllByThemeIdAndMemberIdAndDateBetween(Long themeId, Long memberId, LocalDate dateFrom,
                                                                LocalDate dateTo);

    boolean existsBySlotAndMemberId(Slot slot, Long memberId);

    boolean existsBySlot(Slot slot);

    boolean existsBySlot_ReservationTimeId(Long reservationTimeId);

    boolean existsBySlot_ThemeId(Long id);

}
