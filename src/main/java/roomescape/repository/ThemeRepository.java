package roomescape.repository;

import static roomescape.domain.exception.DomainErrorCode.THEME_NOT_FOUND;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import roomescape.domain.Theme;
import roomescape.domain.exception.RoomEscapeException;

public interface ThemeRepository {

    List<Theme> findAll();

    Optional<Theme> findById(Long id);

    List<Theme> getPopularTop10Themes(LocalDate start, LocalDate end);

    Long save(Theme theme);

    void deleteById(Long id);

    default Theme getById(Long id, String message) {
        return findById(id).orElseThrow(() -> new RoomEscapeException(THEME_NOT_FOUND, message));
    }
}
