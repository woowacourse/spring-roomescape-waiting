package roomescape.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;
import roomescape.domain.Member;
import roomescape.domain.ReservationTime;
import roomescape.domain.Slot;
import roomescape.domain.Theme;
import roomescape.domain.Waiting;

@JdbcTest
@Import({WaitingDao.class, ThemeDao.class, ReservationTimeDao.class})
@Sql("/truncate.sql")
class WaitingDaoTest {

    private static final LocalDate DATE = LocalDate.of(2026, 6, 5);
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 6, 1, 10, 0);

    @Autowired
    private WaitingDao waitingDao;
    @Autowired
    private ThemeDao themeDao;
    @Autowired
    private ReservationTimeDao timeDao;

    private Slot slotA;
    private Slot slotB;

    @BeforeEach
    void setUp() {
        Theme theme = themeDao.save(Theme.create(0, "테마", "url", "설명"));
        ReservationTime time1 = timeDao.save(ReservationTime.create(0, LocalTime.of(10, 0)));
        ReservationTime time2 = timeDao.save(ReservationTime.create(0, LocalTime.of(12, 0)));

        slotA = new Slot(DATE, time1, theme);
        slotB = new Slot(DATE, time2, theme);
    }

    @Test
    @DisplayName("내 대기와 같은 슬롯의 모든 대기(타인 포함)를 반환한다.")
    void findAllSharingSlotWith_sameSlot() {
        Waiting mine = waitingDao.save(Waiting.forNew(new Member("user_e"), slotA, NOW));
        Waiting other = waitingDao.save(Waiting.forNew(new Member("user_b"), slotA, NOW.plusHours(1)));
        waitingDao.save(Waiting.forNew(new Member("user_d"), slotB, NOW));

        List<Waiting> result = waitingDao.findAllSharingSlotWith(new Member("user_e"));

        assertThat(result).extracting(Waiting::id)
                .containsExactlyInAnyOrder(mine.id(), other.id());
    }

    @Test
    @DisplayName("대기가 없는 회원은 빈 목록을 반환한다.")
    void findAllSharingSlotWith_noWaiting() {
        List<Waiting> result = waitingDao.findAllSharingSlotWith(new Member("없는유저"));

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("해당 슬롯에 회원의 대기가 존재하면 true를 반환한다.")
    void existsBySlotAndOwner_true() {
        waitingDao.save(Waiting.forNew(new Member("user_e"), slotA, NOW));

        assertThat(waitingDao.existsBySlotAndOwner(slotA, new Member("user_e"))).isTrue();
    }

    @Test
    @DisplayName("해당 슬롯에 회원의 대기가 없으면 false를 반환한다.")
    void existsBySlotAndOwner_false() {
        assertThat(waitingDao.existsBySlotAndOwner(slotA, new Member("없는유저"))).isFalse();
    }

    @Test
    @DisplayName("같은 슬롯에서 createdAt, id 순으로 가장 앞선 대기를 반환한다.")
    void findNextInLine() {
        Waiting third = waitingDao.save(Waiting.forNew(new Member("third"), slotA, NOW.plusHours(1)));
        Waiting first = waitingDao.save(Waiting.forNew(new Member("first"), slotA, NOW));
        Waiting second = waitingDao.save(Waiting.forNew(new Member("second"), slotA, NOW));
        waitingDao.save(Waiting.forNew(new Member("other-slot"), slotB, NOW.minusHours(1)));

        assertThat(waitingDao.findNextInLine(slotA))
                .hasValueSatisfying(waiting -> assertThat(waiting.id()).isEqualTo(first.id()));

        waitingDao.deleteById(first.id());

        assertThat(waitingDao.findNextInLine(slotA))
                .hasValueSatisfying(waiting -> assertThat(waiting.id()).isEqualTo(second.id()));
        assertThat(waitingDao.findNextInLine(slotA))
                .hasValueSatisfying(waiting -> assertThat(waiting.id()).isNotEqualTo(third.id()));
    }
}
