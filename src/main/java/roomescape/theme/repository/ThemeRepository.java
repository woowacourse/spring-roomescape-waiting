package roomescape.theme.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.dto.PopularThemeRequestDto;

public interface ThemeRepository {

    List<Theme> findAll();

    List<Theme> findThemesOrderByReservationCount(LocalDate from, LocalDate to, PopularThemeRequestDto dto);

    Optional<Theme> findById(Long id);

    Theme findByIdOrThrow(Long id);

    Theme save(Theme theme);

    void deleteById(Long id);
}
