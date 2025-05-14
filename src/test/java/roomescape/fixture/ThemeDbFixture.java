package roomescape.fixture;

import org.springframework.stereotype.Component;
import roomescape.theme.domain.Theme;
import roomescape.theme.service.out.ThemeRepository;

@Component
public class ThemeDbFixture {

    private final ThemeRepository themeRepository;

    public ThemeDbFixture(ThemeRepository themeRepository) {
        this.themeRepository = themeRepository;
    }

    public Theme 공포() {
        Theme theme = Theme.create("공포", "공포 테마", "공포.jpg");
        return themeRepository.save(theme);
    }

    public Theme 커스텀_테마(String customName) {
        Theme theme = Theme.create(customName, "커스텀 테마", "custom.jpg");
        return themeRepository.save(theme);
    }
}
