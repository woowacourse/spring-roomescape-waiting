package roomescape.domain.repository;

import java.time.LocalDate;
import java.util.List;

import roomescape.domain.Theme;
import roomescape.dto.AvailableTimeResponse;

public interface ThemeRepository {
    Long save(String name, String description, String thumbnailUrl);
    List<Theme> findPopularThemes(int size, LocalDate from, LocalDate to);
    List<AvailableTimeResponse> findAvailableTimeById(long themeId, String date);
    List<Theme> findAll();
    void delete(long id);
}
