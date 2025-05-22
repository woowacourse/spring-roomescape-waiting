package roomescape.domain.waiting;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WaitingRepository extends JpaRepository<Waiting, Long> {

    Optional<Waiting> findByDateAndTimeSlotIdAndThemeIdAndUserId(LocalDate date, long timeId, long themeId,
                                                                 long userId);

    List<Waiting> findByUserId(long userId);
}
