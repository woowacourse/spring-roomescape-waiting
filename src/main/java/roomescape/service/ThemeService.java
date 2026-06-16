package roomescape.service;

import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Theme;
import roomescape.exception.DuplicateEntityException;
import roomescape.query.ThemeQueryRepository;
import roomescape.repository.ThemeRepository;
import roomescape.service.command.ThemeRegisterCommand;
import roomescape.service.result.ThemeRegisterResult;
import roomescape.service.result.ThemeTimesResult;

@Service
@RequiredArgsConstructor
public class ThemeService {

    private final ThemeRepository themeRepository;
    private final ThemeQueryRepository themeQueryRepository;

    @Transactional
    public ThemeRegisterResult register(ThemeRegisterCommand command) {
        validateDuplicationName(command.name());

        Theme theme = Theme.create(command.name(), command.description(), command.thumbnailImageUrl(), command.price());

        return ThemeRegisterResult.from(themeRepository.save(theme));
    }

    @Transactional
    public void deactivate(long id) {
        themeRepository.findById(id)
                .ifPresent(existingTheme -> {
                    existingTheme.deactivate();
                    themeRepository.update(existingTheme);
                });
    }

    @Transactional
    public void activate(long id) {
        themeRepository.findById(id)
                .ifPresent(existingTheme -> {
                    existingTheme.activate();
                    themeRepository.update(existingTheme);
                });
    }

    public List<ThemeRegisterResult> getAllThemes() {
        return themeQueryRepository.getAllThemes();
    }

    public List<ThemeRegisterResult> getAllActiveThemes() {
        return themeQueryRepository.getAllActiveThemes();
    }

    public List<ThemeRegisterResult> getPopularThemes(LocalDate startDate, LocalDate endDate) {
        return themeQueryRepository.getPopularThemes(startDate, endDate);
    }

    public List<ThemeTimesResult> getThemeReservationStatus(long themeId, LocalDate date) {
        return themeQueryRepository.getThemeReservationStatus(themeId, date);
    }

    private void validateDuplicationName(String name) {
        if (themeRepository.isActiveByName(name)) {
            throw new DuplicateEntityException("이미 존재하는 테마입니다. 테마 명: %s", name);
        }
    }
}
