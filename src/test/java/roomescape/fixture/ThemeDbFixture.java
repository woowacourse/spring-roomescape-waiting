package roomescape.fixture;

import org.springframework.stereotype.Component;
import roomescape.domain.Theme;
import roomescape.infrastructure.repository.ThemeRepository;

@Component
public class ThemeDbFixture {

    private final ThemeRepository themeRepository;

    public ThemeDbFixture(ThemeRepository themeRepository) {
        this.themeRepository = themeRepository;
    }

    public Theme 공포() {
        String name = "공포";
        String description = "공포 테마";
        String thumbnail = "공포.jpg";

        return themeRepository.save(Theme.create(name, description, thumbnail));
    }

    public Theme 커스텀_테마(String customName) {
        String description = customName + "테마";
        String thumbnail = customName + ".jpg";

        return themeRepository.save(Theme.create(customName, description, thumbnail));
    }
}
