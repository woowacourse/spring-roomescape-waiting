package roomescape.reservation.infra;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.fixture.ReservationFixture;
import roomescape.global.exception.UniqueConstraintViolationException;
import roomescape.reservation.domain.ReservationSlot;
import roomescape.reservation.domain.User;
import roomescape.reservation.domain.Waiting;
import roomescape.reservation.domain.repository.WaitingRepository;
import roomescape.support.TestDataHelper;

@JdbcTest
@Import(JdbcWaitingRepository.class)
class JdbcWaitingRepositoryTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private WaitingRepository waitingRepository;

    private TestDataHelper testHelper;

    @BeforeEach
    void setUp() {
        testHelper = new TestDataHelper(jdbcTemplate);
    }

    @DisplayName("ID로 슬롯 조회를 테스트합니다")
    @Test
    void find_slot_by_id() {
        Long timeId = testHelper.insertReservationTime(LocalTime.of(9, 0));
        Long themeId = testHelper.insertTheme("theme name", "theme description", "theme img url");
        LocalDate date = LocalDate.of(2026, 5, 6);
        Long waitingId = testHelper.insertWaiting(
                "스타크",
                date,
                themeId,
                timeId
        );
        ReservationSlot slot = waitingRepository.findSlotById(waitingId)
                .orElseThrow();

        SoftAssertions.assertSoftly(assertSoftly -> {
            assertSoftly.assertThat(slot.date()).isEqualTo(date);
            assertSoftly.assertThat(slot.themeId()).isEqualTo(themeId);
            assertSoftly.assertThat(slot.timeId()).isEqualTo(timeId);
            assertSoftly.assertThat(slot.startAt()).isEqualTo(LocalTime.of(9, 0));
        });
    }

    @DisplayName("방탈출 예약 대기 추가를 테스트합니다.")
    @Test
    void save_user_reservation() {
        Long timeId = testHelper.insertReservationTime(LocalTime.of(9, 0));
        Long themeId = testHelper.insertTheme("theme name", "theme description", "theme img url");
        User stark = ReservationFixture.userNameStark();

        ReservationSlot slot = ReservationSlot.builder()
                .date(LocalDate.of(2026, 5, 4))
                .themeId(themeId)
                .timeId(timeId)
                .startAt(LocalTime.of(9, 0))
                .build();
        Waiting waiting = Waiting.builder()
                .user(stark)
                .slot(slot)
                .build();

        Waiting savedWaiting = waitingRepository.save(waiting);

        SoftAssertions.assertSoftly(assertSoftly -> {
            assertSoftly.assertThat(savedWaiting.getUser()).isEqualTo(stark);
            assertSoftly.assertThat(savedWaiting.getSlot().date()).isEqualTo(LocalDate.of(2026, 5, 4));
            assertSoftly.assertThat(savedWaiting.getSlot().themeId()).isEqualTo(themeId);
            assertSoftly.assertThat(savedWaiting.getSlot().timeId()).isEqualTo(timeId);
            assertSoftly.assertThat(savedWaiting.getRank().value()).isEqualTo(1);
        });
    }

    @DisplayName("동일한 사용자와 슬롯으로 예약 대기 추가 시 유니크 제약 위반 예외를 테스트합니다.")
    @Test
    void save_duplicate_user_and_slot_exception() {
        Long timeId = testHelper.insertReservationTime(LocalTime.of(9, 0));
        Long themeId = testHelper.insertTheme("theme name", "theme description", "theme img url");
        LocalDate date = LocalDate.of(2026, 5, 4);
        testHelper.insertWaiting("스타크", date, themeId, timeId);
        User stark = ReservationFixture.userNameStark();

        ReservationSlot slot = ReservationSlot.builder()
                .date(date)
                .themeId(themeId)
                .timeId(timeId)
                .startAt(LocalTime.of(9, 0))
                .build();
        Waiting waiting = Waiting.builder()
                .user(stark)
                .slot(slot)
                .build();

        assertThatThrownBy(() -> waitingRepository.save(waiting))
                .isInstanceOf(UniqueConstraintViolationException.class);
    }

    @DisplayName("방탈출 예약 대기 삭제를 테스트합니다.")
    @Test
    void delete_user_reservation() {
        Long themeId = testHelper.insertTheme("테마1", "설명1", "img1.jpg");
        Long timeId = testHelper.insertReservationTime(LocalTime.of(9, 0));
        LocalDate date = LocalDate.of(2026, 5, 6);
        Long waitingId = testHelper.insertWaiting(
                "스타크",
                date,
                themeId,
                timeId
        );

        assertThat(waitingRepository.delete(waitingId)).isEqualTo(1);
    }

    @DisplayName("방탈출 예약 대기 추가 시 다음 순번 저장을 테스트합니다.")
    @Test
    void save_waiting_with_next_rank() {
        Long themeId = testHelper.insertTheme("테마1", "설명1", "img1.jpg");
        Long timeId = testHelper.insertReservationTime(LocalTime.of(9, 0));
        LocalDate date = LocalDate.of(2026, 5, 6);
        testHelper.insertWaiting(
                "스타크",
                date,
                themeId,
                timeId
        );
        testHelper.insertWaiting(
                "피노",
                date,
                themeId,
                timeId
        );
        testHelper.insertWaiting(
                "네오",
                date,
                themeId,
                timeId
        );

        ReservationSlot slot = ReservationSlot.builder()
                .date(date)
                .themeId(themeId)
                .timeId(timeId)
                .startAt(LocalTime.of(9, 0))
                .build();
        User kaya = User.builder()
                .name("카야")
                .build();
        Waiting waiting = Waiting.builder()
                .user(kaya)
                .slot(slot)
                .build();

        Waiting savedWaiting = waitingRepository.save(waiting);

        assertThat(savedWaiting.getRank().value()).isEqualTo(4);
    }

    @DisplayName("대기가 여러개 있을 때 첫번째 대기만 조회해오는 것을 테스트합니다.")
    @Test
    void find_first_waiting() {
        Long themeId = testHelper.insertTheme("테마1", "설명1", "img1.jpg");
        Long timeId = testHelper.insertReservationTime(LocalTime.of(9, 0));
        LocalDate date = LocalDate.of(2026, 5, 6);
        Long firstWaitingId = testHelper.insertWaiting(
                "스타크",
                date,
                themeId,
                timeId
        );
        testHelper.insertWaiting(
                "피노",
                date,
                themeId,
                timeId
        );
        testHelper.insertWaiting(
                "네오",
                date,
                themeId,
                timeId
        );
        User stark = ReservationFixture.userNameStark();
        ReservationSlot slot = ReservationSlot.builder()
                .date(date)
                .themeId(themeId)
                .timeId(timeId)
                .startAt(LocalTime.of(9, 0))
                .build();

        Waiting waiting = waitingRepository.findFirstBySlot(slot)
                .orElseThrow();

        SoftAssertions.assertSoftly(assertSoftly -> {
            assertSoftly.assertThat(waiting.getId()).isEqualTo(firstWaitingId);
            assertSoftly.assertThat(waiting.getUser()).isEqualTo(stark);
            assertSoftly.assertThat(waiting.getSlot()).isEqualTo(slot);
            assertSoftly.assertThat(waiting.getRank().value()).isEqualTo(1);
        });

    }
}
