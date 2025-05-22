package roomescape.business.reader;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.business.dto.ThemeDto;
import roomescape.business.model.entity.Theme;
import roomescape.business.model.repository.Themes;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ThemeReader {

    private final Themes themes;

    public List<ThemeDto> getAll() {
        List<Theme> themes = this.themes.findAll();
        return ThemeDto.fromEntities(themes);
    }

    private static final int AGGREGATE_START_DATE_INTERVAL = 7;
    private static final int AGGREGATE_END_DATE_INTERVAL = 1;

    public List<ThemeDto> getPopular(final int size) {
        LocalDate now = LocalDate.now();
        List<Theme> popularThemes = themes.findPopularThemes(
                now.minusDays(AGGREGATE_START_DATE_INTERVAL),
                now.minusDays(AGGREGATE_END_DATE_INTERVAL),
                size
        );
        return ThemeDto.fromEntities(popularThemes);
    }
}
