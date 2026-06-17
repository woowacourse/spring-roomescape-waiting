package roomescape.waiting.adapter.out.persistence;

import jakarta.persistence.LockModeType;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import roomescape.waiting.application.port.out.projection.WaitingDetailProjection;
import roomescape.waiting.domain.Waiting;

interface SpringDataWaitingRepository extends JpaRepository<Waiting, Long> {
    boolean existsBySlot_IdAndMember_Id(long slotId, long memberId);

    boolean existsBySlot_Id(long slotId);

    List<Waiting> findAllBySlot_IdOrderById(long slotId);

    List<Waiting> findAllBySlot_IdIn(List<Long> slotIds);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT w FROM Waiting w WHERE w.id = :id")
    Optional<Waiting> findByIdForUpdate(@Param("id") long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT w FROM Waiting w WHERE w.slot.id = :slotId ORDER BY w.id")
    List<Waiting> findAllBySlotIdOrderByIdForUpdate(@Param("slotId") long slotId);

    @Query("""
            SELECT w.slot.time.id
            FROM Waiting w
            WHERE w.slot.date = :date
              AND w.slot.theme.id = :themeId
            """)
    List<Long> findTimeIdsByDateAndThemeId(@Param("date") LocalDate date, @Param("themeId") long themeId);

    @Query("""
            SELECT new roomescape.waiting.application.port.out.projection.WaitingDetailProjection(
                w.id,
                w.slot.id,
                w.member.name,
                w.slot.date,
                w.slot.theme.id,
                w.slot.theme.name,
                w.slot.theme.description,
                w.slot.theme.thumbnailUrl,
                w.slot.time.id,
                w.slot.time.startAt
            )
            FROM Waiting w
            ORDER BY w.id
            """)
    List<WaitingDetailProjection> findAllWaitingDetails();

    @Query("""
            SELECT new roomescape.waiting.application.port.out.projection.WaitingDetailProjection(
                w.id,
                w.slot.id,
                w.member.name,
                w.slot.date,
                w.slot.theme.id,
                w.slot.theme.name,
                w.slot.theme.description,
                w.slot.theme.thumbnailUrl,
                w.slot.time.id,
                w.slot.time.startAt
            )
            FROM Waiting w
            WHERE w.member.id = :memberId
            ORDER BY w.id
            """)
    List<WaitingDetailProjection> findAllWaitingDetailsByMemberId(@Param("memberId") long memberId);
}
