package roomescape.service.theme.module;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.theme.Theme;
import roomescape.domain.theme.ThemeName;
import roomescape.dto.theme.ThemeRequest;
import roomescape.repository.ThemeRepository;

@Service
@Transactional
public class ThemeRegisterService {

    private final ThemeRepository themeRepository;

    public ThemeRegisterService(ThemeRepository themeRepository) {
        this.themeRepository = themeRepository;
    }

    public Long resisterTheme(ThemeRequest themeRequest) {
        ThemeName name = new ThemeName(themeRequest.name());
        validateNameDuplicate(name);
        Theme theme = themeRequest.toEntity();

        return themeRepository.save(theme).getId();
    }

    public void validateNameDuplicate(ThemeName name) {
        if (themeRepository.existsByThemeName(name)) {
            throw new IllegalArgumentException(
                    "[ERROR] 동일한 이름의 테마가 존재해 등록할 수 없습니다.",
                    new Throwable("theme_name : " + name)
            );
        }
    }
}
