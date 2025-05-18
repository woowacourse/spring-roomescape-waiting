package roomescape.theme.application;

import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.exception.resource.AlreadyExistException;
import roomescape.exception.resource.ResourceInUseException;
import roomescape.theme.domain.Theme;
import roomescape.theme.infrastructure.ThemeRepository;
import roomescape.theme.ui.dto.CreateThemeRequest;
import roomescape.theme.ui.dto.ThemeResponse;

@Service
@RequiredArgsConstructor
public class ThemeService {

    private final ThemeRepository themeRepository;

    @Transactional
    public ThemeResponse create(final CreateThemeRequest request) {
        if (themeRepository.existsByName(request.name())) {
            throw new AlreadyExistException("해당 테마명이 이미 존재합니다. name = " + request.name());
        }

        final Theme theme = new Theme(request.name(), request.description(), request.thumbnail());
        final Theme saved = themeRepository.save(theme);

        return ThemeResponse.from(saved);
    }

    @Transactional
    public void delete(final Long id) {
        themeRepository.getByIdOrThrow(id);

        try {
            themeRepository.deleteById(id);
        } catch (final DataIntegrityViolationException e) {
            throw new ResourceInUseException("해당 테마를 사용하고 있는 예약이 존재합니다. id = " + id);
        }
    }

    @Transactional(readOnly = true)
    public List<ThemeResponse> findAll() {
        return themeRepository.findAll()
                .stream()
                .map(ThemeResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ThemeResponse> findPopularThemes() {
        final LocalDate dateTo = LocalDate.now();
        final LocalDate dateFrom = dateTo.minusDays(7);
        final int limit = 10;

        return themeRepository.getTopNThemesInPeriod(dateFrom, dateTo, limit)
                .stream()
                .map(ThemeResponse::from)
                .toList();
    }
}
