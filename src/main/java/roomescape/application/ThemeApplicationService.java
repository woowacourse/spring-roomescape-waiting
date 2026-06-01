package roomescape.application;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.api.dto.ThemeRequest;
import roomescape.application.command.ThemeCommandService;
import roomescape.application.query.ThemeQueryService;
import roomescape.domain.Theme;

@Service
@Transactional(readOnly = true)
public class ThemeApplicationService {

    private final ThemeCommandService themeCommandService;
    private final ThemeQueryService themeQueryService;

    public ThemeApplicationService(
            ThemeCommandService themeCommandService,
            ThemeQueryService themeQueryService
    ) {
        this.themeCommandService = themeCommandService;
        this.themeQueryService = themeQueryService;
    }

    @Transactional
    public Theme save(ThemeRequest request) {
        Theme theme = new Theme(
                request.name(),
                request.description(),
                request.thumbnailImageUrl()
        );

        return themeCommandService.save(theme);
    }

    public List<Theme> findAll() {
        return themeQueryService.findAll();
    }

    public List<Theme> findPopular(LocalDate now, Integer days, Integer limit) {
        return themeQueryService.findPopular(now, days, limit);
    }

    @Transactional
    public void delete(Long id) {
        themeCommandService.delete(id);
    }
}
