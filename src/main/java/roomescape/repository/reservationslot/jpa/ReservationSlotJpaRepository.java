package roomescape.repository.reservationslot.jpa;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationSlotJpaRepository extends JpaRepository<ReservationSlotJpaEntity, Long> {

    @Override
    @EntityGraph(attributePaths = {"theme", "time"})
    List<ReservationSlotJpaEntity> findAll();

    @Override
    @EntityGraph(attributePaths = {"theme", "time"})
    Optional<ReservationSlotJpaEntity> findById(Long id);

    @EntityGraph(attributePaths = {"theme", "time"})
    List<ReservationSlotJpaEntity> findAllByDateAndTheme_Id(LocalDate date, Long themeId);

    @EntityGraph(attributePaths = {"theme", "time"})
    Optional<ReservationSlotJpaEntity> findByDateAndTheme_IdAndTime_Id(
            LocalDate date,
            Long themeId,
            Long timeId
    );
}
