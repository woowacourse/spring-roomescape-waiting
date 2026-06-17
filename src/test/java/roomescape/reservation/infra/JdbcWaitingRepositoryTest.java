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
import org.springframework.boot.jdbc.test.autoconfigure.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.fixture.ReservationFixture;
import roomescape.global.exception.UniqueConstraintViolationException;
import roomescape.reservation.domain.Rank;
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

    @DisplayName("ID로 예약 대기 조회를 테스트합니다.")
    @Test
    void find_by_id() {
        Long timeId = testHelper.insertReservationTime(LocalTime.of(9, 0));
        Long themeId = testHelper.insertTheme("theme name", "theme description", "theme img url");
        LocalDate date = LocalDate.of(2026, 5, 6);
        Long waitingId = testHelper.insertWaiting(
                "스타크",
                date,
                themeId,
                timeId
        );
        Waiting waiting = waitingRepository.findById(waitingId)
                .orElseThrow();

        SoftAssertions.assertSoftly(assertSoftly -> {
            assertSoftly.assertThat(waiting.getId()).isEqualTo(waitingId);
            assertSoftly.assertThat(waiting.getUser().name()).isEqualTo("스타크");
            assertSoftly.assertThat(waiting.getSlot().date()).isEqualTo(date);
            assertSoftly.assertThat(waiting.getSlot().themeId()).isEqualTo(themeId);
            assertSoftly.assertThat(waiting.getSlot().timeId()).isEqualTo(timeId);
            assertSoftly.assertThat(waiting.getSlot().startAt()).isEqualTo(LocalTime.of(9, 0));
            assertSoftly.assertThat(waiting.getRank().value()).isEqualTo(1);
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

    @DisplayName("예약 대기 순번 재정렬 시 같은 슬롯의 뒤 순번을 당기는 것을 테스트합니다.")
    @Test
    void rebalance_rank() {
        Long themeId = testHelper.insertTheme("테마1", "설명1", "img1.jpg");
        Long timeId = testHelper.insertReservationTime(LocalTime.of(9, 0));
        LocalDate date = LocalDate.of(2026, 5, 6);
        testHelper.insertWaiting(
                "스타크",
                date,
                themeId,
                timeId
        );
        Long waitingId = testHelper.insertWaiting(
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

        Waiting waiting = waitingRepository.findById(waitingId)
                .orElseThrow();
        ReservationSlot slot = waiting.getSlot();
        waitingRepository.delete(waitingId);
        waitingRepository.rebalanceRank(waiting.getSlot(), waiting.getRank());
        Integer starkRank = testHelper.findWaitingRank("스타크", slot);
        Integer neoRank = testHelper.findWaitingRank("네오", slot);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(starkRank).isEqualTo(1);
            softly.assertThat(neoRank).isEqualTo(2);
        });
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

    @DisplayName("슬롯의 예약 대기 개수 조회를 테스트합니다.")
    @Test
    void count_by_slot() {
        Long themeId = testHelper.insertTheme("테마1", "설명1", "img1.jpg");
        Long timeId = testHelper.insertReservationTime(LocalTime.of(9, 0));
        LocalDate date = LocalDate.of(2026, 5, 6);
        testHelper.insertWaiting("스타크", date, themeId, timeId);
        testHelper.insertWaiting("피노", date, themeId, timeId);
        testHelper.insertWaiting("피케이", date.plusDays(1), themeId, timeId);
        ReservationSlot slot = ReservationSlot.builder()
                .date(date)
                .themeId(themeId)
                .timeId(timeId)
                .startAt(LocalTime.of(9, 0))
                .build();

        int totalRankCount = waitingRepository.countBySlot(slot);

        assertThat(totalRankCount).isEqualTo(2);
    }

    @DisplayName("대기 순번 미루기 저장 시 대상 순번을 변경하고 사이 순번을 당기는 것을 테스트합니다.")
    @Test
    void postpone() {
        Long themeId = testHelper.insertTheme("테마1", "설명1", "img1.jpg");
        Long timeId = testHelper.insertReservationTime(LocalTime.of(9, 0));
        LocalDate date = LocalDate.of(2026, 5, 6);
        Long waitingId = testHelper.insertWaiting("스타크", date, themeId, timeId);
        testHelper.insertWaiting("피노", date, themeId, timeId);
        testHelper.insertWaiting("네오", date, themeId, timeId);
        testHelper.insertWaiting("피케이", date.plusDays(1), themeId, timeId);
        ReservationSlot slot = ReservationSlot.builder()
                .date(date)
                .themeId(themeId)
                .timeId(timeId)
                .startAt(LocalTime.of(9, 0))
                .build();
        User stark = ReservationFixture.userNameStark();
        Waiting waiting = Waiting.builder()
                .id(waitingId)
                .user(stark)
                .slot(slot)
                .rank(Rank.builder()
                        .value(1)
                        .build())
                .build();
        Waiting postponedWaiting = waiting.withRank(Rank.builder()
                .value(3)
                .build());

        Integer updatedCount = waitingRepository.postpone(waiting, postponedWaiting);
        Integer starkRank = testHelper.findWaitingRank("스타크", slot);
        Integer pinoRank = testHelper.findWaitingRank("피노", slot);
        Integer neoRank = testHelper.findWaitingRank("네오", slot);
        ReservationSlot otherSlot = ReservationSlot.builder()
                .date(date.plusDays(1))
                .themeId(themeId)
                .timeId(timeId)
                .startAt(LocalTime.of(9, 0))
                .build();
        Integer kayaRank = testHelper.findWaitingRank("피케이", otherSlot);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(updatedCount).isEqualTo(1);
            softly.assertThat(starkRank).isEqualTo(3);
            softly.assertThat(pinoRank).isEqualTo(1);
            softly.assertThat(neoRank).isEqualTo(2);
            softly.assertThat(kayaRank).isEqualTo(1);
        });
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
