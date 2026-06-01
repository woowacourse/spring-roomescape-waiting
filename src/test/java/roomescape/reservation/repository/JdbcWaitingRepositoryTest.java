package roomescape.reservation.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.reservation.domain.Waiting;
import roomescape.reservation.domain.repository.WaitingRepository;
import roomescape.reservation.domain.repository.dto.WaitingDetail;
import roomescape.reservation.domain.repository.dto.WaitingOrderDetail;
import roomescape.reservation.infra.JdbcWaitingRepository;
import roomescape.support.RepositoryTestHelper;

@JdbcTest
public class JdbcWaitingRepositoryTest {

    WaitingRepository waitingRepository;
    RepositoryTestHelper testHelper;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        waitingRepository = new JdbcWaitingRepository(jdbcTemplate);
        testHelper = new RepositoryTestHelper(jdbcTemplate);
    }

    @DisplayName("사용자의 방탈출 대기 추가를 테스트합니다.")
    @Test
    void save_user_waiting() {
        Long timeId = testHelper.insertReservationTime(LocalTime.of(9, 0));
        Long themeId = testHelper.insertTheme("theme name", "theme description", "theme img url");

        Waiting waiting = Waiting.builder()
                .name("name")
                .date(LocalDate.of(2026, 5, 4))
                .themeId(themeId)
                .timeId(timeId)
                .build();

        Waiting savedWaiting = waitingRepository.save(waiting);

        SoftAssertions.assertSoftly(assertSoftly -> {
            assertSoftly.assertThat(savedWaiting.getId()).isPositive();
            assertSoftly.assertThat(savedWaiting.getName()).isEqualTo("name");
            assertSoftly.assertThat(savedWaiting.getDate()).isEqualTo(LocalDate.of(2026, 5, 4));
            assertSoftly.assertThat(savedWaiting.getThemeId()).isEqualTo(themeId);
            assertSoftly.assertThat(savedWaiting.getTimeId()).isEqualTo(timeId);
        });
    }

    @DisplayName("대기 id로 대기 디테일 정보 가져오기를 테스트합니다.")
    @Test
    void find_detail_by_id() {
        Long timeId = testHelper.insertReservationTime(LocalTime.of(9, 0));
        Long themeId = testHelper.insertTheme("theme name", "theme description", "theme img url");

        Waiting waiting = Waiting.builder()
                .name("name")
                .date(LocalDate.of(2026, 5, 4))
                .themeId(themeId)
                .timeId(timeId)
                .build();

        Waiting savedWaiting = waitingRepository.save(waiting);
        WaitingDetail waitingDetail = waitingRepository.findDetailById(savedWaiting.getId()).get();

        SoftAssertions.assertSoftly(assertSoftly -> {
            assertSoftly.assertThat(waitingDetail.waitingId()).isEqualTo(savedWaiting.getId());
            assertSoftly.assertThat(waitingDetail.username()).isEqualTo("name");
            assertSoftly.assertThat(waitingDetail.date()).isEqualTo(LocalDate.of(2026, 5, 4));
            assertSoftly.assertThat(waitingDetail.themeId()).isEqualTo(themeId);
            assertSoftly.assertThat(waitingDetail.themeName()).isEqualTo("theme name");
            assertSoftly.assertThat(waitingDetail.themeDescription()).isEqualTo("theme description");
            assertSoftly.assertThat(waitingDetail.thumbnailImgUrl()).isEqualTo("theme img url");
            assertSoftly.assertThat(waitingDetail.timeId()).isEqualTo(timeId);
            assertSoftly.assertThat(waitingDetail.startAt()).isEqualTo(LocalTime.of(9, 0));
        });
    }

    @DisplayName("같은 날짜, 테마, 시간의 가장 오래된 대기 조회를 테스트합니다.")
    @Test
    void find_oldest_by_date_and_theme_id_and_time_id() {
        Long timeId = testHelper.insertReservationTime(LocalTime.of(9, 0));
        Long themeId = testHelper.insertTheme("theme name", "theme description", "theme img url");
        LocalDate date = LocalDate.of(2026, 5, 4);

        Waiting firstWaiting = waitingRepository.save(Waiting.builder()
                .name("first")
                .date(date)
                .themeId(themeId)
                .timeId(timeId)
                .build());
        waitingRepository.save(Waiting.builder()
                .name("second")
                .date(date)
                .themeId(themeId)
                .timeId(timeId)
                .build());

        Waiting oldestWaiting = waitingRepository.findOldestByDateAndThemeIdAndTimeId(date, themeId, timeId).get();

        assertThat(oldestWaiting.getId()).isEqualTo(firstWaiting.getId());
    }

    @DisplayName("대기 삭제를 테스트합니다.")
    @Test
    void delete_waiting() {
        Long timeId = testHelper.insertReservationTime(LocalTime.of(9, 0));
        Long themeId = testHelper.insertTheme("theme name", "theme description", "theme img url");
        Waiting savedWaiting = waitingRepository.save(Waiting.builder()
                .name("name")
                .date(LocalDate.of(2026, 5, 4))
                .themeId(themeId)
                .timeId(timeId)
                .build());

        Integer deletedCount = waitingRepository.delete(savedWaiting.getId());

        SoftAssertions.assertSoftly(assertSoftly -> {
            assertSoftly.assertThat(deletedCount).isEqualTo(1);
            assertSoftly.assertThat(waitingRepository.findDetailById(savedWaiting.getId())).isEmpty();
        });
    }

    @DisplayName("이름으로 대기 목록과 대기 순번 조회를 테스트합니다.")
    @Test
    void find_by_name() {
        Long timeId = testHelper.insertReservationTime(LocalTime.of(9, 0));
        Long themeId = testHelper.insertTheme("theme name", "theme description", "theme img url");
        LocalDate date = LocalDate.of(2026, 5, 4);

        waitingRepository.save(Waiting.builder()
                .name("other")
                .date(date)
                .themeId(themeId)
                .timeId(timeId)
                .build());
        Waiting savedWaiting = waitingRepository.save(Waiting.builder()
                .name("name")
                .date(date)
                .themeId(themeId)
                .timeId(timeId)
                .build());

        List<WaitingOrderDetail> waitingOrderDetails = waitingRepository.findByName("name");

        SoftAssertions.assertSoftly(assertSoftly -> {
            assertSoftly.assertThat(waitingOrderDetails).hasSize(1);
            assertSoftly.assertThat(waitingOrderDetails.getFirst().waitingId()).isEqualTo(savedWaiting.getId());
            assertSoftly.assertThat(waitingOrderDetails.getFirst().username()).isEqualTo("name");
            assertSoftly.assertThat(waitingOrderDetails.getFirst().date()).isEqualTo(date);
            assertSoftly.assertThat(waitingOrderDetails.getFirst().themeId()).isEqualTo(themeId);
            assertSoftly.assertThat(waitingOrderDetails.getFirst().themeName()).isEqualTo("theme name");
            assertSoftly.assertThat(waitingOrderDetails.getFirst().themeDescription()).isEqualTo("theme description");
            assertSoftly.assertThat(waitingOrderDetails.getFirst().thumbnailImgUrl()).isEqualTo("theme img url");
            assertSoftly.assertThat(waitingOrderDetails.getFirst().timeId()).isEqualTo(timeId);
            assertSoftly.assertThat(waitingOrderDetails.getFirst().startAt()).isEqualTo(LocalTime.of(9, 0));
            assertSoftly.assertThat(waitingOrderDetails.getFirst().order()).isEqualTo(2);
        });
    }

    @DisplayName("여러 대기 순번이 존재할 때 순번이 제대로 나오는지 테스트합니다.")
    @Test
    void find_by_name_returns_correct_order() {
        Long timeId = testHelper.insertReservationTime(LocalTime.of(9, 0));
        Long themeId = testHelper.insertTheme("theme name", "theme description", "theme img url");
        LocalDate date = LocalDate.of(2026, 5, 4);

        Waiting firstWaiting = waitingRepository.save(Waiting.builder()
                .name("other")
                .date(date)
                .themeId(themeId)
                .timeId(timeId)
                .build());
        Waiting savedWaiting = waitingRepository.save(Waiting.builder()
                .name("카야")
                .date(date)
                .themeId(themeId)
                .timeId(timeId)
                .build());

        List<WaitingOrderDetail> waitingOrderDetails = waitingRepository.findByName("카야");

        SoftAssertions.assertSoftly(assertSoftly -> {
            assertSoftly.assertThat(firstWaiting.getId()).isPositive();
            assertSoftly.assertThat(firstWaiting.getId()).isLessThan(savedWaiting.getId());
            assertSoftly.assertThat(waitingOrderDetails).hasSize(1);
            assertSoftly.assertThat(waitingOrderDetails.getFirst().waitingId()).isEqualTo(savedWaiting.getId());
            assertSoftly.assertThat(waitingOrderDetails.getFirst().username()).isEqualTo("카야");
            assertSoftly.assertThat(waitingOrderDetails.getFirst().date()).isEqualTo(date);
            assertSoftly.assertThat(waitingOrderDetails.getFirst().themeId()).isEqualTo(themeId);
            assertSoftly.assertThat(waitingOrderDetails.getFirst().themeName()).isEqualTo("theme name");
            assertSoftly.assertThat(waitingOrderDetails.getFirst().themeDescription()).isEqualTo("theme description");
            assertSoftly.assertThat(waitingOrderDetails.getFirst().thumbnailImgUrl()).isEqualTo("theme img url");
            assertSoftly.assertThat(waitingOrderDetails.getFirst().timeId()).isEqualTo(timeId);
            assertSoftly.assertThat(waitingOrderDetails.getFirst().startAt()).isEqualTo(LocalTime.of(9, 0));
            assertSoftly.assertThat(waitingOrderDetails.getFirst().order()).isEqualTo(2);
        });
    }

    @DisplayName("다른 테마의 대기가 있어도 같은 테마 대기의 순번만 계산된다")
    @Test
    void find_by_name_returns_order_excluding_waiting_with_other_theme() {
        Long timeId = testHelper.insertReservationTime(LocalTime.of(9, 0));
        Long themeId = testHelper.insertTheme("theme name", "theme description", "theme img url");
        Long otherThemeId = testHelper.insertTheme("other theme", "other description", "other img url");
        LocalDate date = LocalDate.of(2026, 5, 4);

        waitingRepository.save(Waiting.builder()
                .name("other")
                .date(date)
                .themeId(themeId)
                .timeId(timeId)
                .build());

        waitingRepository.save(Waiting.builder()
                .name("other theme waiting")
                .date(date)
                .themeId(otherThemeId)
                .timeId(timeId)
                .build());

        Waiting savedWaiting = waitingRepository.save(Waiting.builder()
                .name("name")
                .date(date)
                .themeId(themeId)
                .timeId(timeId)
                .build());

        List<WaitingOrderDetail> waitingOrderDetails = waitingRepository.findByName("name");

        SoftAssertions.assertSoftly(assertSoftly -> {
            assertSoftly.assertThat(waitingOrderDetails).hasSize(1);
            assertSoftly.assertThat(waitingOrderDetails.getFirst().waitingId()).isEqualTo(savedWaiting.getId());
            assertSoftly.assertThat(waitingOrderDetails.getFirst().themeId()).isEqualTo(themeId);
            assertSoftly.assertThat(waitingOrderDetails.getFirst().order()).isEqualTo(2);
        });
    }
}
