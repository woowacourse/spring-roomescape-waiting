package roomescape.theme.service.usecase;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.theme.domain.Theme;
import roomescape.theme.repository.ThemeRepository;
import roomescape.theme.service.converter.ThemeConverter;
import roomescape.theme.service.dto.CreateThemeServiceRequest;

@RequiredArgsConstructor
@Service
@Transactional
public class ThemeCommandUseCase {

    private final ThemeRepository themeRepository;

    public Theme create(final CreateThemeServiceRequest createThemeServiceRequest) {
        return themeRepository.save(ThemeConverter.toDomain(createThemeServiceRequest));
    }

    public void delete(final Long id) {
        themeRepository.deleteById(id);
    }
}
