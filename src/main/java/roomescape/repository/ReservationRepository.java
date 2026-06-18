package roomescape.repository;

import jakarta.persistence.LockModeType;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationSlot;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByMember_Id(Long memberId);

    List<Reservation> findBySlot_Theme_IdAndSlot_Date(Long themeId, LocalDate date);

    boolean existsBySlot(ReservationSlot slot);

    boolean existsBySlotAndIdNot(ReservationSlot slot, Long id);

    boolean existsByMemberAndSlot(Member member, ReservationSlot slot);

    @Query("SELECT COUNT(r) > 0 FROM Reservation r WHERE r.slot.time.id = :timeId")
    boolean existsByTimeId(@Param("timeId") long timeId);

    @Query("SELECT COUNT(r) > 0 FROM Reservation r WHERE r.slot.theme.id = :themeId")
    boolean existsByThemeId(@Param("themeId") long themeId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM Reservation r WHERE r.slot.date = :#{#slot.date} AND r.slot.time = :#{#slot.time} AND r.slot.theme = :#{#slot.theme}")
    Optional<Reservation> findBySlotForUpdate(@Param("slot") ReservationSlot slot);
}
