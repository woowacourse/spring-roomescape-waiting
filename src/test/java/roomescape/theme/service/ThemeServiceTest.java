package roomescape.theme.service;


import java.util.List;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import roomescape.common.exception.DataExistException;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.repository.ThemeRepository;

@DataJpaTest
class ThemeServiceTest {

    @Autowired
    private ThemeService themeService;

    @Autowired
    private ThemeRepository themeRepository;

    @Test
    void 테마를_저장한다() {
        // given
        final String name = "우가우가";
        final String description = "우가우가 설명";
        final String thumbnail = "따봉우가.jpg";

        // when & then
        Assertions.assertThatCode(() -> {
            themeService.save(name, description, thumbnail);
        }).doesNotThrowAnyException();
    }

    @Test
    void 테마를_삭제한다() {
        // given
        final String name = "우가우가";
        final String description = "우가우가 설명";
        final String thumbnail = "따봉우가.jpg";
        final Theme theme = new Theme(name, description, thumbnail);
        final Theme savedTheme = themeRepository.save(theme);

        // when & then
        Assertions.assertThatCode(() -> {
            themeService.deleteById(savedTheme.getId());
        }).doesNotThrowAnyException();
    }

    @Test
    void 테마를_조회한다() {
        // given
        final String name = "우가우가";
        final String description = "우가우가 설명";
        final String thumbnail = "따봉우가.jpg";
        final Theme theme = new Theme(name, description, thumbnail);
        final Theme savedTheme = themeRepository.save(theme);

        // when
        final Theme found = themeService.getById(savedTheme.getId());

        // then
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(found.getName()).isEqualTo(name);
            softly.assertThat(found.getDescription()).isEqualTo(description);
            softly.assertThat(found.getThumbnail()).isEqualTo(thumbnail);
        });
    }

    @Test
    void 테마_전체를_조회한다() {
        // when
        final List<Theme> themes = themeService.findAll();

        // then
        Assertions.assertThat(themes).hasSize(0);
    }

    @Test
    void 테마_이름은_중복_될_수_없다() {
        // given
        final String name = "우가우가";
        final String description = "우가우가 설명";
        final String thumbnail = "따봉우가.jpg";
        final Theme theme = new Theme(name, description, thumbnail);
        themeRepository.save(theme);

        // when & then
        Assertions.assertThatThrownBy(() -> {
            themeService.save(name, description, thumbnail);
        }).isInstanceOf(DataExistException.class);
    }

    @TestConfiguration
    static class TestConfig {

        @Bean
        public ThemeService themeService(
                final ThemeRepository themeRepository,
                final ReservationRepository reservationRepository
        ) {
            return new ThemeService(themeRepository, reservationRepository);
        }
    }
}
