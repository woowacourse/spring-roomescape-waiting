package roomescape.domain.theme;

import roomescape.domain.RoomEscapeException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static roomescape.domain.DomainErrorCode.RESOURCE_NOT_FOUND;

public interface ThemeRepository {
    Theme save(Theme theme);

    List<Theme> findAll();

    Optional<Theme> findById(long themeId);

    List<Theme> findFamous(long days, LocalDate date, long limit);

    void deleteById(long themeId);

    boolean existsById(long themeId);

    default Theme getById(long id) {
        return findById(id)
                .orElseThrow(() -> new RoomEscapeException(RESOURCE_NOT_FOUND, "해당 테마를 찾을 수 없습니다. : " + id));
    }
}
