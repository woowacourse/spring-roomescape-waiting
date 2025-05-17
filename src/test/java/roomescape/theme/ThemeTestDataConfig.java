package roomescape.theme;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import roomescape.theme.domain.Theme;
import roomescape.theme.fixture.ThemeFixture;
import roomescape.theme.repository.ThemeRepository;

@TestConfiguration
public class ThemeTestDataConfig {

    private static final String NAME_FIELD = "dummyName";
    private static final String DESCRIPTION_FIELD = "dummyDescription";
    private static final String THUMBNAIL_FILED = "dummyThumbnail";

    @Autowired
    private ThemeRepository repository;

    private Long savedId;
    private Theme savedTheme;

    @PostConstruct
    public void setUpTestData() {
        Theme theme = ThemeFixture.create(NAME_FIELD, DESCRIPTION_FIELD, THUMBNAIL_FILED);
        savedTheme = repository.save(theme);
        savedId = savedTheme.getId();
    }

    public Long getSavedId() {
        return savedId;
    }

    public Theme getSavedTheme() {
        return savedTheme;
    }
}
