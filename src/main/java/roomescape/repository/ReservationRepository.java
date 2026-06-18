package roomescape.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import roomescape.domain.Reservation;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    @Query(
            value = """
                    SELECT r FROM Reservation r 
                    WHERE r.slot.reservationDate = :reservationDate 
                      AND r.slot.time.id = :timeId 
                      AND r.slot.theme.id = :themeId
                    """)
    @EntityGraph(attributePaths = {"member", "slot.time", "slot.theme"})
    Optional<Reservation> findBySlot(
            @Param("reservationDate") LocalDate reservationDate,
            @Param("timeId") Long timeId,
            @Param("themeId") Long themeId);

    @EntityGraph(attributePaths = {"member", "slot.time", "slot.theme"})
    List<Reservation> findByMemberId(Long memberId);

    @EntityGraph(attributePaths = {"member", "slot.time", "slot.theme"})
    List<Reservation> findByMember_Name(String name);

    boolean existsBySlot_Time_Id(Long timeId);

    boolean existsBySlot_Theme_Id(Long themeId);
}
