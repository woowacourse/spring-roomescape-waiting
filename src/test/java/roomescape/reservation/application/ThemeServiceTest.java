package roomescape.reservation.application;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;
import roomescape.reservation.application.dto.CreateReservationRequest;
import roomescape.reservation.application.repository.ReservationRepository;
import roomescape.reservation.application.repository.ReservationTimeRepository;
import roomescape.reservation.application.repository.ThemeRepository;
import roomescape.reservation.application.service.ReservationTimeService;
import roomescape.reservation.application.service.ThemeService;
import roomescape.reservation.domain.ReservationDate;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.Theme;
import roomescape.reservation.infrastructure.dao.ReservationDao;
import roomescape.reservation.infrastructure.dao.ReservationTimeDao;
import roomescape.reservation.infrastructure.dao.ThemeDao;
import roomescape.reservation.presentation.dto.ReservationTimeRequest;
import roomescape.reservation.presentation.dto.ThemeRequest;
import roomescape.reservation.presentation.dto.ThemeResponse;

@JdbcTest
public class ThemeServiceTest {

    @Autowired
    private ThemeService themeService;

    @Autowired
    private ReservationTimeService reservationTimeService;

    @Autowired
    private ReservationRepository reservationRepository;

    @Test
    @DisplayName("테마 추가 테스트")
    void createThemeTest() {
        // given
        ThemeRequest themeRequest = new ThemeRequest(
                "레벨2 탈출",
                "우테코 레벨2를 탈출하는 내용입니다.",
                "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg"
        );

        // when
        ThemeResponse theme = themeService.createTheme(themeRequest);

        // then
        assertThat(theme.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("테마 전체 조회 테스트")
    void getThemesTest() {
        // given
        ThemeRequest themeRequest = new ThemeRequest(
                "레벨2 탈출",
                "우테코 레벨2를 탈출하는 내용입니다.",
                "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg"
        );
        themeService.createTheme(themeRequest);

        // when
        List<ThemeResponse> themes = themeService.getThemes();

        // then
        assertThat(themes).hasSize(1);
    }

    @Test
    @DisplayName("테마 삭제 테스트")
    void deleteThemeTest() {
        // given
        ThemeRequest themeRequest = new ThemeRequest(
                "레벨2 탈출",
                "우테코 레벨2를 탈출하는 내용입니다.",
                "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg"
        );
        ThemeResponse theme = themeService.createTheme(themeRequest);

        // when
        themeService.deleteTheme(theme.getId());

        // then
        assertThat(themeService.getThemes()).hasSize(0);
    }

    @Test
    @DisplayName("저장되어 있지 않은 id로 요청을 보내면 예외가 발생한다.")
    void deleteExceptionTest() {
        assertThatThrownBy(() -> themeService.deleteTheme(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("이미 삭제되어 있는 리소스입니다.");
    }

    @Test
    @DisplayName("인기 테마 조회 테스트")
    void getPopularThemesTest() {
        // given
        ThemeRequest themeRequest = new ThemeRequest(
                "레벨2 탈출",
                "우테코 레벨2를 탈출하는 내용입니다.",
                "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg"
        );
        themeService.createTheme(themeRequest);

        ThemeRequest themeRequest2 = new ThemeRequest(
                "레벨3 탈출",
                "우테코 레벨3를 탈출하는 내용입니다.",
                "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg"
        );
        ThemeResponse theme = themeService.createTheme(themeRequest2);

        ReservationTimeRequest reservationTimeRequest = new ReservationTimeRequest(LocalTime.of(15, 40));
        reservationTimeService.createReservationTime(reservationTimeRequest);

        reservationRepository.insert(new CreateReservationRequest(
                new Member(2L, "admin@admin.com", "admin", "어드민", Role.ADMIN),
                new Theme(theme.getId(), "레벨3 탈출",
                        "우테코 레벨3를 탈출하는 내용입니다.",
                        "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg"),
                new ReservationDate(LocalDate.now().minusDays(3)),
                new ReservationTime(1L, LocalTime.of(15, 40))
        ));

        // when
        final List<ThemeResponse> popularThemes = themeService.getPopularThemes();

        // then
        assertThat(popularThemes.stream()
                .map(ThemeResponse::getId)
                .toList()
        ).isEqualTo(List.of(theme.getId()));
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        public ThemeRepository themeRepository(
                final JdbcTemplate jdbcTemplate
        ) {
            return new ThemeDao(jdbcTemplate) {
            };
        }

        @Bean
        public ReservationRepository reservationRepository(
                final JdbcTemplate jdbcTemplate
        ) {
            return new ReservationDao(jdbcTemplate) {
            };
        }

        @Bean
        public ReservationTimeRepository reservationTimeRepository(
                final JdbcTemplate jdbcTemplate
        ) {
            return new ReservationTimeDao(jdbcTemplate) {
            };
        }

        @Bean
        public ThemeService themeService(
                final ReservationRepository reservationRepository,
                final ThemeRepository themeRepository
        ) {
            return new ThemeService(reservationRepository, themeRepository);
        }

        @Bean
        public ReservationTimeService reservationTimeService(
                final ReservationRepository reservationRepository,
                final ReservationTimeRepository reservationTimeRepository
        ) {
            return new ReservationTimeService(reservationTimeRepository, reservationRepository);
        }
    }
}
