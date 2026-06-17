package roomescape.reservation.infra;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.reservation.application.dao.WaitingDetailDao;
import roomescape.reservation.application.dto.WaitingDetail;
import roomescape.support.TestDataHelper;

@JdbcTest
@Import(JdbcWaitingDao.class)
class JdbcWaitingDaoTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private WaitingDetailDao waitingDao;

    private TestDataHelper testHelper;

    @BeforeEach
    void setUp() {
        testHelper = new TestDataHelper(jdbcTemplate);
    }

    @DisplayName("사용자의 이름으로 해당 사용자의 대기 상세 정보와 순번 조회를 테스트합니다.")
    @Test
    void find_by_name() {
        Long themeId = testHelper.insertTheme("테마1", "설명1", "img1.jpg");
        Long tenTimeId = testHelper.insertReservationTime(LocalTime.of(10, 0));
        Long elevenTimeId = testHelper.insertReservationTime(LocalTime.of(11, 0));
        LocalDate date = LocalDate.of(2026, 5, 10);

        testHelper.insertWaiting(
                "스타크",
                date,
                themeId,
                tenTimeId
        );
        testHelper.insertWaiting(
                "비밥",
                date,
                themeId,
                tenTimeId
        );
        testHelper.insertWaiting(
                "스타크",
                date,
                themeId,
                elevenTimeId
        );

        List<WaitingDetail> details = waitingDao.findByName("스타크");
        WaitingDetail first = details.getFirst();
        WaitingDetail second = details.get(1);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(details).hasSize(2);
            softly.assertThat(first.username()).isEqualTo("스타크");
            softly.assertThat(first.date()).isEqualTo(date);
            softly.assertThat(first.themeId()).isEqualTo(themeId);
            softly.assertThat(first.timeId()).isEqualTo(tenTimeId);
            softly.assertThat(first.startAt()).isEqualTo(LocalTime.of(10, 0));
            softly.assertThat(first.rank()).isEqualTo(1);
            softly.assertThat(first.totalRankCount()).isEqualTo(2);
            softly.assertThat(second.timeId()).isEqualTo(elevenTimeId);
            softly.assertThat(second.rank()).isEqualTo(1);
            softly.assertThat(second.totalRankCount()).isEqualTo(1);
        });
    }
}
