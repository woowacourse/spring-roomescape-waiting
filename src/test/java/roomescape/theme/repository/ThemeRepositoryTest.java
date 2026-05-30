package roomescape.theme.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.ThemeFactory;

@JdbcTest(properties = "spring.sql.init.data-locations=")
@Import({JdbcThemeRepository.class, ThemeFactory.class})
class ThemeRepositoryTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private ThemeFactory themeFactory;

    private Long themeAId;
    private Long themeBId;
    private Long themeCId;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("INSERT INTO reservation_time (start_at, finish_at) VALUES ('10:00', '11:00')");
        jdbcTemplate.update("INSERT INTO member (name, email, password) VALUES ('user1', 'user1@test.com', '1234')");
        jdbcTemplate.update("INSERT INTO theme (name, description, image_url) VALUES ('테마A', '설명A', 'https://a.com')");
        jdbcTemplate.update("INSERT INTO theme (name, description, image_url) VALUES ('테마B', '설명B', 'https://b.com')");
        jdbcTemplate.update("INSERT INTO theme (name, description, image_url) VALUES ('테마C', '설명C', 'https://c.com')");
        jdbcTemplate.update("INSERT INTO theme (name, description, image_url) VALUES ('테마D', '설명D', 'https://d.com')");

        Long memberId = jdbcTemplate.queryForObject("SELECT id FROM member WHERE email = 'user1@test.com'", Long.class);
        Long timeId = jdbcTemplate.queryForObject("SELECT id FROM reservation_time WHERE start_at = '10:00:00'", Long.class);
        themeAId = jdbcTemplate.queryForObject("SELECT id FROM theme WHERE name = '테마A'", Long.class);
        themeBId = jdbcTemplate.queryForObject("SELECT id FROM theme WHERE name = '테마B'", Long.class);
        themeCId = jdbcTemplate.queryForObject("SELECT id FROM theme WHERE name = '테마C'", Long.class);

        LocalDate past = LocalDate.now().minusDays(1);
        jdbcTemplate.update("INSERT INTO reservation (member_id, date, time_id, theme_id) VALUES (?, ?, ?, ?)", memberId, past, timeId, themeAId);
        jdbcTemplate.update("INSERT INTO reservation (member_id, date, time_id, theme_id) VALUES (?, ?, ?, ?)", memberId, past.minusDays(1), timeId, themeAId);
        jdbcTemplate.update("INSERT INTO reservation (member_id, date, time_id, theme_id) VALUES (?, ?, ?, ?)", memberId, past.minusDays(2), timeId, themeAId);
        jdbcTemplate.update("INSERT INTO reservation (member_id, date, time_id, theme_id) VALUES (?, ?, ?, ?)", memberId, past, timeId, themeBId);
        jdbcTemplate.update("INSERT INTO reservation (member_id, date, time_id, theme_id) VALUES (?, ?, ?, ?)", memberId, past.minusDays(1), timeId, themeBId);
        jdbcTemplate.update("INSERT INTO reservation (member_id, date, time_id, theme_id) VALUES (?, ?, ?, ?)", memberId, past, timeId, themeCId);
    }

    @DisplayName("테마를 저장하면 ID가 부여된다.")
    @Test
    void 테마_저장_성공() {
        Theme saved = themeRepository.save(themeFactory.create("테마E", "설명E", "https://e.com"));
        assertThat(saved.getId()).isNotNull().isPositive();
    }

    @DisplayName("ID로 테마를 조회하면 해당 테마 정보가 반환된다.")
    @Test
    void ID로_테마_조회_성공() {
        assertThat(themeRepository.findById(themeAId)).isPresent();
    }

    @DisplayName("전체 테마 목록을 조회한다.")
    @Test
    void 전체_테마_조회() {
        assertThat(themeRepository.findAll()).hasSize(4);
    }

    @DisplayName("테마를 삭제하면 더 이상 조회되지 않는다.")
    @Test
    void 테마_삭제_성공() {
        Theme saved = themeRepository.save(themeFactory.create("테마E", "설명E", "https://e.com"));
        themeRepository.deleteById(saved.getId());
        assertThat(themeRepository.findById(saved.getId())).isEmpty();
    }

    @DisplayName("예약이 있는 테마면 예약 존재 여부가 true다.")
    @Test
    void 예약_존재하는_테마() {
        assertThat(themeRepository.existsReservationByThemeId(themeAId)).isTrue();
    }

    @DisplayName("기간 내 예약 수 기준으로 인기 테마 ID를 내림차순 조회한다.")
    @Test
    void 인기_테마_ID_조회() {
        List<Long> ids = themeRepository.findTopThemeIds(
                LocalDate.now().minusDays(6), LocalDate.now().minusDays(1), 10);
        assertThat(ids).containsExactly(themeAId, themeBId, themeCId);
    }

    @DisplayName("ID 목록으로 테마를 일괄 조회한다.")
    @Test
    void IDs로_테마_일괄_조회() {
        List<Theme> themes = themeRepository.findAllByIds(List.of(themeAId, themeBId, themeCId));
        assertThat(themes).hasSize(3);
    }
}
