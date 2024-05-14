package roomescape.theme.domain;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.repository.CrudRepository;

public interface ThemeRepository extends CrudRepository<Theme, Long> {

    
    List<Theme> findByPeriodOrderByReservationCount(LocalDate start, LocalDate end, int count);
}
