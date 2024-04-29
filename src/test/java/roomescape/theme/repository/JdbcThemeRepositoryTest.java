package roomescape.theme.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static roomescape.time.fixture.DateTimeFixture.DAY_AFTER_TOMORROW;
import static roomescape.time.fixture.DateTimeFixture.TODAY;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.ThemeRepository;

// 테스트 초기 데이터
// 테마1: 잠실 캠퍼스 탈출 - 예약 2
// 테마2: 선릉 캠퍼스 탈출 - 예약 1
// 테마3: 강박 탈출 - 예약 0
@JdbcTest
@Sql(scripts = "/test_data.sql", executionPhase = ExecutionPhase.BEFORE_TEST_CLASS)
class JdbcThemeRepositoryTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private ThemeRepository themeRepository;

    @BeforeEach
    void setUp() {
        themeRepository = new JdbcThemeRepository(jdbcTemplate);
    }

    @DisplayName("테마 전체를 조회할 수 있다")
    @Test
    void should_find_all() {
        assertThat(themeRepository.findAll()).hasSize(3);
    }

    @DisplayName("원하는 ID의 테마를 조회할 수 있다")
    @Test
    void should_get_theme_when_id_is_given() {
        assertThat(themeRepository.findById(1L)).isPresent();
    }

    @DisplayName("테마를 추가할 수 있다")
    @Test
    void should_insert_theme() {
        Theme theme = new Theme(null, "리비", "멋짐", "url");
        Theme saved = themeRepository.save(theme);

        assertThat(saved.getId()).isNotNull();
    }

    @DisplayName("주어진 기간 중 예약이 가장 많은 순으로 테마를 정해진 개수만큼 가지고 올 수 있다")
    @Test
    void should_get_popular_theme() {
        Theme popularTheme = new Theme(1L,
                "잠실 캠퍼스 탈출",
                "미션을 빨리 진행하고 잠실 캠퍼스를 탈출하자!",
                "https://velog.velcdn.com/images/jangws/post/cfe0e548-1242-470d-bfa8-19eeb72debc5/image.jpg");

        assertThat(themeRepository.findByPeriodOrderByReservationCount(TODAY, DAY_AFTER_TOMORROW, 1))
                .hasSize(1)
                .containsExactly(popularTheme);
    }

    @DisplayName("원하는 ID의 테마를 삭제할 수 있다")
    @Test
    void should_deleteById() {
        themeRepository.deleteById(3L);
        assertThat(themeRepository.findAll()).hasSize(2);
    }
}
