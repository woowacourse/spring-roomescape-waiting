package roomescape.reservation.infrastructure.dao;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;
import roomescape.reservation.application.dto.CreateReservationRequest;
import roomescape.reservation.domain.ReservationDate;
import roomescape.reservation.domain.Theme;
import roomescape.reservation.presentation.dto.ReservationTimeRequest;
import roomescape.reservation.presentation.dto.ThemeRequest;

@JdbcTest
public class ThemeDaoTest {

    private final ReservationDao reservationDao;
    private final ReservationTimeDao reservationTimeDao;
    private final ThemeDao themeDao;
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public ThemeDaoTest(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.reservationDao = new ReservationDao(jdbcTemplate);
        this.reservationTimeDao = new ReservationTimeDao(jdbcTemplate);
        this.themeDao = new ThemeDao(jdbcTemplate);
    }

    @BeforeEach
    void resetAutoIncrement() {
        jdbcTemplate.execute("ALTER TABLE theme ALTER COLUMN id RESTART WITH 1");
    }

    @Test
    @DisplayName("테마 추가 확인 테스트")
    void insertTest() {
        // given
        ThemeRequest themeRequest = new ThemeRequest(
                "레벨2 탈출",
                "우테코 레벨2를 탈출하는 내용입니다.",
                "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg"
        );

        // when
        themeDao.insert(themeRequest);

        // then
        assertThat(count()).isEqualTo(1);
    }

    @Test
    @DisplayName("테마 전체 조회 확인 테스트")
    void getThemesTest() {
        // given
        ThemeRequest themeRequest = new ThemeRequest(
                "레벨2 탈출",
                "우테코 레벨2를 탈출하는 내용입니다.",
                "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg"
        );

        themeDao.insert(themeRequest);

        // when
        List<Theme> allThemes = themeDao.findAllThemes();

        // then
        assertThat(allThemes).hasSize(1);
    }

    @Test
    @DisplayName("테마 삭제 확인 테스트")
    void deleteTest() {
        // when
        themeDao.delete(0L);

        // then
        assertThat(count()).isEqualTo(0);
    }

    @Test
    @DisplayName("인기 테마 조회 테스트")
    void findPopularThemesTest() {
        // given
        ReservationTimeRequest reservationTimeRequest = new ReservationTimeRequest(LocalTime.of(15, 40));
        ThemeRequest themeRequest1 = new ThemeRequest(
                "레벨2 탈출",
                "우테코 레벨2를 탈출하는 내용입니다.",
                "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg"
        );
        ThemeRequest themeRequest2 = new ThemeRequest(
                "레벨3 탈출",
                "우테코 레벨3를 탈출하는 내용입니다.",
                "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg"
        );
        final Theme theme1 = themeDao.insert(themeRequest1);
        final Theme theme2 = themeDao.insert(themeRequest2);

        CreateReservationRequest createReservationRequest = new CreateReservationRequest(
                new Member(2L, "admin@admin.com", "admin", "어드민", Role.ADMIN),
                theme2,
                new ReservationDate(LocalDate.now().minusDays(3)),
                reservationTimeDao.insert(reservationTimeRequest.getStartAt())
        );
        reservationDao.insert(createReservationRequest);

        // when
        final List<Theme> popularThemes = themeDao.findPopularThemes();

        // then
        assertThat(popularThemes.stream()
                .map(Theme::getId)
                .toList()
        ).isEqualTo(List.of(2L));
    }

    private int count() {
        String sql = "select count(*) from theme";
        return jdbcTemplate.queryForObject(sql, Integer.class);
    }
}
