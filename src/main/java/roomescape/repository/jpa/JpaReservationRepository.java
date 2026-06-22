package roomescape.repository.jpa;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.domain.Reservation;

public interface JpaReservationRepository extends JpaRepository<Reservation, Long> {

    @EntityGraph(attributePaths = {"theme", "time"})
    List<Reservation> findByName(String name);

    boolean existsByDateAndTime_IdAndTheme_Id(LocalDate date, Long timeId, Long themeId);

    List<Reservation> findReservationsByTheme_IdAndDate(Long themeId, LocalDate date);

    boolean existsByTheme_Id(Long themeId);

    boolean existsByTime_Id(Long timeId);

    boolean existsByNameAndDateAndTime_IdAndTheme_Id(
            String name,
            LocalDate date,
            Long timeId,
            Long themeId
    );
}
