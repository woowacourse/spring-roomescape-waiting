package roomescape.domain.theme;

import roomescape.common.exception.NotFoundException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ThemeRepository {
    Theme save(Theme theme);
    List<Theme> findAll();
    Optional<Theme> findById(long themeId);
    List<Theme> findFamous(long days, LocalDate date, long limit);
    void deleteById(long themeId);
    boolean existsById(long themeId);

    default Theme getById(long id) {
        return findById(id)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 테마입니다. 입력을 확인해 주세요."));
    }
}
