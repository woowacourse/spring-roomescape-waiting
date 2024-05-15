package roomescape.theme.domain;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ThemeRepository extends JpaRepository<Theme, Long> {


    List<Theme> findByPeriodOrderByReservationCount(LocalDate start, LocalDate end, int count);
}
