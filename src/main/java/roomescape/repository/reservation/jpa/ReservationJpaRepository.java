package roomescape.repository.reservation.jpa;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationJpaRepository extends JpaRepository<ReservationJpaEntity, Long> {

    @Override
    @EntityGraph(attributePaths = {"slot", "slot.theme", "slot.time"})
    List<ReservationJpaEntity> findAll();

    @Override
    @EntityGraph(attributePaths = {"slot", "slot.theme", "slot.time"})
    Optional<ReservationJpaEntity> findById(Long id);

    @EntityGraph(attributePaths = {"slot", "slot.theme", "slot.time"})
    Optional<ReservationJpaEntity> findBySlot_DateAndSlot_Theme_IdAndSlot_Time_Id(
            LocalDate date,
            Long themeId,
            Long timeId
    );

    @EntityGraph(attributePaths = {"slot", "slot.theme", "slot.time"})
    List<ReservationJpaEntity> findAllBySlot_DateAndSlot_Theme_Id(LocalDate date, Long themeId);

    boolean existsBySlot_Time_Id(Long timeId);
}
