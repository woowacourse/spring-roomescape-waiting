package roomescape.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static roomescape.config.FixedClockConfig.TODAY;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import roomescape.dao.dto.TimeQueryResult;
import roomescape.domain.reservation.theme.Theme;

@JdbcTest
@Import({ThemeDao.class, ReservationTimeDao.class})
class ThemeDaoTest {

    @Autowired
    private ThemeDao themeDao;

    @Autowired
    private ReservationTimeDao reservationTimeDao;

    @Test
    @DisplayName("특정 테마와 날짜에 대해 예약되지 않은 시간 목록만 조회한다.")
    void 예약되지_않은_시간_조회_테스트() {
        // given
        Long themeId = 1L;
        LocalDate date = LocalDate.parse(TODAY).minusDays(7);
        boolean expected = false;

        // when
        List<TimeQueryResult> availableTimes = reservationTimeDao.findStatuesByThemeIdAndDate(themeId, date);

        // then
        assertThat(availableTimes.get(5).isReserved()).isEqualTo(expected);
    }

    @Test
    @DisplayName("최근 7일간 예약이 많은 순서대로 테마를 조회한다.")
    void 최근_7일간_예약이_많은_테마_조회_테스트() {
        List<String> expectedNames = List.of(
                "우테코 공포물", "미래 도시", "고대 이집트", "우주 탐험", "마법 학교",
                "해저 왕국", "좀비 아포칼립스", "탐정 사무소", "시간 여행", "서부 개척시대"
        );
        LocalDate today = LocalDate.parse(TODAY);

        List<Theme> topThemes = themeDao.findPopularThemes(10, today);

        // then
        assertThat(topThemes).hasSize(expectedNames.size());
        for (int i = 0; i < expectedNames.size(); i++) {
            assertThat(topThemes.get(i).getName().value()).isEqualTo(expectedNames.get(i));
        }
    }
}
