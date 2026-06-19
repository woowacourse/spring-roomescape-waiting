package roomescape.theme.infra;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.support.TestDataHelper;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.repository.ThemeRepository;

@JdbcTest
@Import(JdbcThemeRepository.class)
public class JdbcThemeRepositoryTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ThemeRepository themeRepository;

    private TestDataHelper testHelper;

    @BeforeEach
    void setUp() {
        testHelper = new TestDataHelper(jdbcTemplate);
    }

    @DisplayName("db의 정상 저장을 테스트 합니다.")
    @Test
    void save_successfully() {
        Theme theme = Theme.builder()
                .name("테마1")
                .description("설명1")
                .thumbnailImgUrl("img1.jpg")
                .build();

        Theme savedTheme = themeRepository.save(theme);

        SoftAssertions.assertSoftly(assertSoftly -> {
            assertSoftly.assertThat(savedTheme).isNotNull();
            assertSoftly.assertThat(savedTheme.getId()).isPositive();
            assertSoftly.assertThat(savedTheme.getName()).isEqualTo("테마1");
            assertSoftly.assertThat(savedTheme.getDescription()).isEqualTo("설명1");
            assertSoftly.assertThat(savedTheme.getThumbnailImgUrl()).isEqualTo("img1.jpg");
        });
    }

    @DisplayName("db에 특정 테마가 존재하지 않는 것을 테스트 합니다.")
    @Test
    void check_none_exists_successfully() {
        Theme theme = Theme.builder()
                .name("테마1")
                .description("설명1")
                .thumbnailImgUrl("img1.jpg")
                .build();

        Boolean alreadyExists = themeRepository.existsByNameAndDescription(theme);

        assertThat(alreadyExists).isFalse();
    }

    @DisplayName("db에 특정 테마가 존재하는 것을 테스트 합니다.")
    @Test
    void check_exists_successfully() {
        testHelper.insertTheme("테마1", "설명1", "img1.jpg");

        Theme theme = Theme.builder()
                .name("테마1")
                .description("설명1")
                .thumbnailImgUrl("img1.jpg")
                .build();
        Boolean alreadyExists = themeRepository.existsByNameAndDescription(theme);
        assertThat(alreadyExists).isTrue();
    }

    @DisplayName("db에서 테마 삭제를 테스트 합니다.")
    @Test
    void delete_theme_successfully() {
        Long themeId = testHelper.insertTheme("테마1", "설명1", "img1.jpg");

        assertThat(themeRepository.delete(themeId)).isEqualTo(1);
    }

    @DisplayName("db에서 테마를 전체 조회합니다.")
    @Test
    void find_all_themes() {
        testHelper.insertTheme("테마1", "설명1", "img1.jpg");
        testHelper.insertTheme("테마2", "설명2", "img2.jpg");
        testHelper.insertTheme("테마3", "설명3", "img3.jpg");

        assertThat(themeRepository.findAll().size()).isEqualTo(3);
    }
}
