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
import roomescape.reservation.application.dao.ReservationDetailDao;
import roomescape.reservation.application.dto.ReservationDetail;
import roomescape.support.TestDataHelper;

@JdbcTest
@Import(JdbcReservationDao.class)
class JdbcReservationDaoTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ReservationDetailDao reservationDao;

    private TestDataHelper testHelper;

    @BeforeEach
    void setUp() {
        testHelper = new TestDataHelper(jdbcTemplate);
    }

    @DisplayName("예약 상세 정보 전체 조회를 테스트합니다.")
    @Test
    void find_all() {
        Long themeId = testHelper.insertTheme("테마1", "설명1", "img1.jpg");
        Long tenTimeId = testHelper.insertReservationTime(LocalTime.of(10, 0));
        Long elevenTimeId = testHelper.insertReservationTime(LocalTime.of(11, 0));
        LocalDate date = LocalDate.of(2026, 5, 10);
        testHelper.insertReservation(
                "스타크",
                date,
                themeId,
                tenTimeId
        );
        testHelper.insertReservation(
                "비밥",
                date,
                themeId,
                elevenTimeId
        );

        List<ReservationDetail> details = reservationDao.findAllByPage(1, 0);
        ReservationDetail first = details.getFirst();

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(details).hasSize(1);
            softly.assertThat(reservationDao.countAll()).isEqualTo(2);
            softly.assertThat(first.username()).isEqualTo("스타크");
            softly.assertThat(first.date()).isEqualTo(date);
            softly.assertThat(first.themeId()).isEqualTo(themeId);
            softly.assertThat(first.timeId()).isEqualTo(tenTimeId);
            softly.assertThat(first.startAt()).isEqualTo(LocalTime.of(10, 0));
        });
    }

    @DisplayName("사용자의 이름으로 해당 사용자의 예약 상세 정보 조회를 테스트합니다.")
    @Test
    void find_by_name() {
        Long themeId = testHelper.insertTheme("테마1", "설명1", "img1.jpg");
        Long tenTimeId = testHelper.insertReservationTime(LocalTime.of(10, 0));
        Long elevenTimeId = testHelper.insertReservationTime(LocalTime.of(11, 0));
        LocalDate date = LocalDate.of(2026, 5, 10);
        testHelper.insertReservation(
                "스타크",
                date,
                themeId,
                tenTimeId
        );
        testHelper.insertReservation(
                "비밥",
                date,
                themeId,
                elevenTimeId
        );

        List<ReservationDetail> details = reservationDao.findByName("비밥");
        ReservationDetail first = details.getFirst();

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(details).hasSize(1);
            softly.assertThat(first.username()).isEqualTo("비밥");
            softly.assertThat(first.date()).isEqualTo(date);
            softly.assertThat(first.themeId()).isEqualTo(themeId);
            softly.assertThat(first.timeId()).isEqualTo(elevenTimeId);
            softly.assertThat(first.startAt()).isEqualTo(LocalTime.of(11, 0));
        });
    }
}
