package roomescape.reservation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.controller.dto.request.ThemeSaveRequest;
import roomescape.reservation.controller.dto.response.ThemeDeleteResponse;
import roomescape.reservation.controller.dto.response.ThemeResponse;
import roomescape.reservation.domain.Theme;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.repository.ThemeRepository;

@SpringBootTest
@Transactional
class ThemeServiceTest {

    @Autowired
    private ThemeService themeService;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @DisplayName("테마를 성공적으로 저장한다.")
    @Test
    void saveTheme() {
        // given
        ThemeSaveRequest saveRequest = new ThemeSaveRequest("새 테마", "새 테마 설명", "새 테마 썸네일");

        // when
        ThemeResponse response = themeService.save(saveRequest);

        // then
        assertNotNull(response);
        assertEquals("새 테마", response.name());
        assertTrue(themeRepository.findById(response.themeId()).isPresent());
    }

    @DisplayName("모든 테마를 조회한다.")
    @Test
    void getAllThemes() {
        // when
        List<ThemeResponse> responses = themeService.getAll();

        // then
        assertNotNull(responses);
        assertEquals(4, responses.size());
        assertThat(responses).extracting(ThemeResponse::name)
                .containsExactlyInAnyOrder("링", "도시괴담", "콜러", "제로");
    }

    @DisplayName("인기 테마를 조회한다.")
    @Test
    void findPopularThemes() {
        // given
        LocalDate now = LocalDate.now();

        String sql = "insert into reservation(date, time_id, theme_id, member_id) values (?, ?, ?, ?)";
        jdbcTemplate.update(sql, now, 1L, 1L, 1L);
        jdbcTemplate.update(sql, now.minusDays(3), 1L, 1L, 1L);
        jdbcTemplate.update(sql, now.minusDays(5), 1L, 2L, 1L);

        // when
        List<ThemeResponse> responses = themeService.findPopularThemes();

        // then
        assertNotNull(responses);
        assertThat(responses.size()).isEqualTo(2);
        assertThat(responses.get(0).themeId()).isEqualTo(2);
        assertThat(responses.get(1).themeId()).isEqualTo(1);
    }

    @DisplayName("테마 ID로 테마를 삭제한다.")
    @Test
    void deleteTheme() {
        // given
        Theme theme = themeRepository.save(new Theme("삭제할 테마", "설명", "썸네일"));
        long themeId = theme.getId();

        // when
        ThemeDeleteResponse response = themeService.delete(themeId);

        // then
        assertNotNull(response);
        assertEquals(1, response.updateCount());
    }

    @DisplayName("존재하지 않는 테마 ID로 삭제 시 예외를 던진다.")
    @Test
    void deleteNonExistingThemeThrowsException() {
        // given
        long nonExistingId = 999L;

        // when & then
        assertThrows(NoSuchElementException.class, () -> themeService.delete(nonExistingId));
    }
}
