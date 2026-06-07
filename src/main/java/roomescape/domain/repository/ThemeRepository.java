package roomescape.domain.repository;

import java.time.LocalDate;
import java.util.List;

import roomescape.domain.Theme;
import roomescape.dto.AvailableTimeResponse;

public interface ThemeRepository {
    List<Theme> findPopularThemes(int size, LocalDate from, LocalDate to);
    List<AvailableTimeResponse> findAvailableTimeById(long themeId, String date);
    List<Theme> findAll();
    Long save(String name, String description, String thumbnailUrl);
    void delete(long id);
}
