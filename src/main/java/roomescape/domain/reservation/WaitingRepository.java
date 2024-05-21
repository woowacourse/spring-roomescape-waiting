package roomescape.domain.reservation;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.domain.reservation.dto.WaitingReadOnly;

public interface WaitingRepository extends JpaRepository<Waiting, Long> {

    @Query("select w.member.id from Waiting w where w.id = :id")
    Long findMemberIdById(Long id);

    @Query("""
            select new roomescape.domain.reservation.dto.WaitingReadOnly(
            w.id,
            w.member,
            w.reservation.slot
            )
            from Waiting w""")
    List<WaitingReadOnly> findAllReadOnly();

    @Query("""
            select w
            from Waiting w
            where w.member = :member and w.reservation.slot.date >= :date
            """)
    List<Waiting> findByMemberAndDateAfter(Member member, LocalDate date);

    @Query("select count(w) from Waiting w where w.reservation = :reservation and w.id <= :id")
    Long countRank(Reservation reservation, Long id);

    @EntityGraph(attributePaths = {"reservation"})
    List<Waiting> findByReservation_Slot_DateAfter(LocalDate parse);
}
