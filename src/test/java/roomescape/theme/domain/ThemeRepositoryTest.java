package roomescape.theme.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import roomescape.theme.domain.fixture.ThemeFixture;
import roomescape.support.datasource.ThemeDataSource;
import roomescape.support.datasource.BaseRepositoryTest;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.ThemeRepository;

class ThemeRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private ThemeRepository themeRepository;
    @Autowired
    private ThemeDataSource dataSource;

    @BeforeEach
    void setUp() {
        dataSource.clearTable();
        dataSource.clearId();
    }

    @Test
    void 테마를_저장하고_ID로_조회한다() {
        // given
        Theme theme = ThemeFixture.createDefaultTheme();

        // when
        Theme saved = themeRepository.save(theme);

        // then
        assertThat(saved.getId()).isEqualTo(1L);
        assertThat(saved.getName()).isEqualTo(theme.getName());
        assertThat(saved.getDescription()).isEqualTo(theme.getDescription());
        assertThat(saved.getThumbnailImageUrl()).isEqualTo(theme.getThumbnailImageUrl());
    }

    @Test
    void 동일한_활성_테마_이름으로_저장하면_DB_제약조건_에러가_발생한다() {
        // given
        themeRepository.save(ThemeFixture.createDefaultTheme());

        // when & then
        assertThatThrownBy(() -> themeRepository.save(ThemeFixture.createDefaultTheme()))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void 활성_테마만_페이징_조회한다() {
        // given
        Theme first = themeRepository.save(ThemeFixture.createDefaultTheme());
        themeRepository.save(Theme.create("놀이동산테마", "https://image.com/image2.png", "즐거운 테마입니다."));
        themeRepository.update(first.deactivate());

        // when
        List<Theme> themes = themeRepository.findAll(0, 10);

        // then
        assertThat(themes).hasSize(1).extracting(Theme::getName).containsExactly("놀이동산테마");
    }

    @Test
    void 예약_수_기준으로_인기_테마를_조회한다() {
        // given
        dataSource.insertThemesByCount(2);
        dataSource.insertTimeByStartToEndWithOneHourRotation(10, 12);
        dataSource.insertReservedReservationByTheme(1L, 2);
        dataSource.insertReservedReservationByTheme(2L, 1);

        // when
        List<Theme> themes = themeRepository.findByReservationCountWithLimit(LocalDate.now().minusDays(1),
                LocalDate.now().plusDays(1), 1);

        // then
        assertThat(themes).hasSize(1).extracting(Theme::getId).containsExactly(1L);
    }
}
