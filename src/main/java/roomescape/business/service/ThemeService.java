package roomescape.business.service;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.business.dto.ThemeDto;
import roomescape.business.model.entity.Theme;
import roomescape.business.model.repository.ReservationRepository;
import roomescape.business.model.repository.ThemeRepository;
import roomescape.business.model.vo.Id;
import roomescape.exception.business.NotFoundException;
import roomescape.exception.business.RelatedEntityExistException;

import java.time.LocalDate;
import java.util.List;

import static roomescape.exception.ErrorCode.RESERVED_THEME;
import static roomescape.exception.ErrorCode.THEME_NOT_EXIST;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ThemeService {

    private static final int AGGREGATE_START_DATE_INTERVAL = 7;
    private static final int AGGREGATE_END_DATE_INTERVAL = 1;

    private final ThemeRepository themeRepository;
    private final ReservationRepository reservationRepository;

    @Transactional
    public ThemeDto addAndGet(final String name, final String description, final String thumbnail) {
        val theme = Theme.create(name, description, thumbnail);
        themeRepository.save(theme);
        return ThemeDto.fromEntity(theme);
    }

    public List<ThemeDto> getAll() {
        val themes = themeRepository.findAll();
        return ThemeDto.fromEntities(themes);
    }

    public List<ThemeDto> getPopular(final int size) {
        val now = LocalDate.now();
        val popularThemes = themeRepository.findPopularThemes(
                now.minusDays(AGGREGATE_START_DATE_INTERVAL),
                now.minusDays(AGGREGATE_END_DATE_INTERVAL),
                size
        );
        return ThemeDto.fromEntities(popularThemes);
    }

    @Transactional
    public void delete(final String themeIdValue) {
        val themeId = Id.create(themeIdValue);
        if (reservationRepository.existByThemeId(themeId)) {
            throw new RelatedEntityExistException(RESERVED_THEME);
        }
        if (!themeRepository.existById(themeId)) {
            throw new NotFoundException(THEME_NOT_EXIST);
        }
        themeRepository.deleteById(themeId);
    }
}
