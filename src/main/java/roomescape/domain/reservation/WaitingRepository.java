package roomescape.domain.reservation;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.domain.reservation.dto.WaitingReadOnly;

public interface WaitingRepository extends JpaRepository<Waiting, Long> {

    @Query("""
            select new roomescape.domain.reservation.dto.WaitingReadOnly(
            w.id,
            w.member,
            w.reservation.slot
            )
            from Waiting w""")
    List<WaitingReadOnly> findAllReadOnly();

    @EntityGraph(attributePaths = {"reservation"})
    List<Waiting> findByReservation_Slot_DateGreaterThanEqual(LocalDate parse);
}
