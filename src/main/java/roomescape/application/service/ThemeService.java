package roomescape.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.application.service.command.ThemeRegisterCommand;
import roomescape.application.service.result.ThemeRegisterResult;
import roomescape.domain.Theme;
import roomescape.exception.DuplicateEntityException;
import roomescape.persistence.ThemeRepository;

@Service
@RequiredArgsConstructor
public class ThemeService {

    private final ThemeRepository themeRepository;

    @Transactional
    public ThemeRegisterResult register(ThemeRegisterCommand command) {
        validateDuplicationName(command.name());

        Theme theme = new Theme(command.name(), command.description(), command.thumbnailImageUrl(), command.price());

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

    private void validateDuplicationName(String name) {
        if (themeRepository.isActiveByName(name)) {
            throw new DuplicateEntityException("이미 존재하는 테마입니다. 테마 명: %s", name);
        }
    }
}
