package roomescape.business.application_service.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.business.dto.ThemeDto;
import roomescape.business.model.entity.Theme;
import roomescape.business.model.repository.Reservations;
import roomescape.business.model.repository.Themes;
import roomescape.business.model.vo.Id;
import roomescape.exception.business.NotFoundException;
import roomescape.exception.business.RelatedEntityExistException;

import static roomescape.exception.ErrorCode.RESERVED_THEME;
import static roomescape.exception.ErrorCode.THEME_NOT_EXIST;

@Service
@RequiredArgsConstructor
@Transactional
public class ThemeService {

    private final Themes themes;
    private final Reservations reservations;

    public ThemeDto addAndGet(final String name, final String description, final String thumbnail) {
        Theme theme = new Theme(name, description, thumbnail);
        themes.save(theme);
        return ThemeDto.fromEntity(theme);
    }

    public void delete(final String themeIdValue) {
        Id themeId = Id.create(themeIdValue);
        if (reservations.existByThemeId(themeId)) {
            throw new RelatedEntityExistException(RESERVED_THEME);
        }
        if (!themes.existById(themeId)) {
            throw new NotFoundException(THEME_NOT_EXIST);
        }
        themes.deleteById(themeId);
    }
}
