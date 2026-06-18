package roomescape.repository;

import jakarta.persistence.LockModeType;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.Slot;
import roomescape.domain.Theme;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Reservation> findWithLockById(Long id);

    Optional<Reservation> findBySlot(Slot slot);

    List<Reservation> findBySlot_DateAndSlot_Theme(LocalDate date, Theme theme);

    // JPA 2단계
    List<Reservation> findByReserver(Member reserver);

    @EntityGraph(attributePaths = {
            "slot.time",
            "slot.theme"
    })
    List<Reservation> findAllByReserver(Member reserver);
}
