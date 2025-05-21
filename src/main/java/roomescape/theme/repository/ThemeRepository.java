package roomescape.theme.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import roomescape.reservation.domain.ReservationDate;
import roomescape.theme.domain.Theme;

public interface ThemeRepository {

    List<Theme> findAll();

    Optional<Theme> findById(Long id);

    Theme save(Theme theme);

    void deleteById(Long id);

    List<Theme> getRanking(ReservationDate startDate, ReservationDate endDate, Pageable pageable);
}
