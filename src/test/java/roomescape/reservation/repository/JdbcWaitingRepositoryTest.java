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

    @DisplayName("대기 id로 대기 정보 가져오기를 테스트합니다.")
    @Test
    void find_by_id() {
        Long timeId = testHelper.insertReservationTime(LocalTime.of(9, 0));
        Long themeId = testHelper.insertTheme("theme name", "theme description", "theme img url");

        Waiting waiting = Waiting.builder()
                .name("name")
                .date(LocalDate.of(2026, 5, 4))
                .themeId(themeId)
                .timeId(timeId)
                .build();

        Waiting savedWaiting = waitingRepository.save(waiting);
        Waiting found = waitingRepository.findById(savedWaiting.getId()).get();

        SoftAssertions.assertSoftly(assertSoftly -> {
            assertSoftly.assertThat(found.getId()).isEqualTo(savedWaiting.getId());
            assertSoftly.assertThat(found.getName()).isEqualTo("name");
            assertSoftly.assertThat(found.getDate()).isEqualTo(LocalDate.of(2026, 5, 4));
            assertSoftly.assertThat(found.getThemeId()).isEqualTo(themeId);
            assertSoftly.assertThat(found.getTimeId()).isEqualTo(timeId);
        });
    }

    @DisplayName("같은 슬롯의 대기가 여러 개일 때 가장 오래된 대기만 삭제됩니다.")
    @Test
    void delete_oldest_by_slot() {
        Long timeId = testHelper.insertReservationTime(LocalTime.of(9, 0));
        Long themeId = testHelper.insertTheme("theme name", "theme description", "theme img url");
        LocalDate date = LocalDate.of(2026, 5, 4);

        Waiting firstWaiting = waitingRepository.save(Waiting.builder()
                .name("first")
                .date(date)
                .themeId(themeId)
                .timeId(timeId)
                .build());
        Waiting secondWaiting = waitingRepository.save(Waiting.builder()
                .name("second")
                .date(date)
                .themeId(themeId)
                .timeId(timeId)
                .build());

        waitingRepository.deleteOldestBySlot(date, themeId, timeId);

        assertThat(waitingRepository.findById(firstWaiting.getId())).isEmpty();
        assertThat(waitingRepository.findById(secondWaiting.getId())).isPresent();
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

        waitingRepository.delete(savedWaiting.getId());

        assertThat(waitingRepository.findById(savedWaiting.getId())).isEmpty();
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

        List<WaitingDetail> waitingOrderDetails = waitingRepository.findByName("name");

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
}
