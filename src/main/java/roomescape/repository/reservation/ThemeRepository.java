package roomescape.repository.reservation;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import roomescape.domain.reservation.Theme;

@Repository
public interface ThemeRepository {
    long add(Theme theme);

    List<Theme> findAll();

    Optional<Theme> findById(Long id);

    void deleteById(Long id);
}
