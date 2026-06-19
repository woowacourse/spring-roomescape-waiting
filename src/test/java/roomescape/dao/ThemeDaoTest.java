package roomescape.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static roomescape.common.config.FixedClockConfig.TODAY;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import roomescape.dao.dto.TimeQueryResult;
import roomescape.domain.slot.theme.Theme;

@JdbcTest
@Import({ThemeDao.class, ReservationTimeDao.class})
class ThemeDaoTest {

    @Autowired
    private ThemeDao themeDao;

    @Autowired
    private ReservationTimeDao reservationTimeDao;

    @Test
    @DisplayName("특정 테마와 날짜에 대해 예약되지 않은 시간 목록만 조회한다.")
    void findUnreservedTimes_ByThemeAndDate_ReturnUnreservedTimes() {
        Long themeId = 1L;
        LocalDate date = LocalDate.parse(TODAY).minusDays(7);

        List<TimeQueryResult> availableTimes = reservationTimeDao.findStatuesByThemeIdAndDate(themeId, date);

        assertThat(availableTimes.get(6).isReserved()).isFalse();
    }

    @Test
    @DisplayName("최근 7일간 예약이 많은 순서대로 테마를 조회한다.")
    void findPopularThemes_ForLast7Days_ReturnThemesInOrder() {
        List<String> expectedNames = List.of(
                "우테코 공포물", "미래 도시", "고대 이집트", "우주 탐험", "마법 학교",
                "해저 왕국", "좀비 아포칼립스", "탐정 사무소", "시간 여행", "서부 개척시대"
        );
        LocalDate today = LocalDate.parse(TODAY);

        List<Theme> topThemes = themeDao.findPopularThemes(10, today);

        assertThat(topThemes).hasSize(expectedNames.size());
        for (int i = 0; i < expectedNames.size(); i++) {
            assertThat(topThemes.get(i).getName().value()).isEqualTo(expectedNames.get(i));
        }
    }
}
