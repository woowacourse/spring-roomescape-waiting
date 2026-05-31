package roomescape.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
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
@Import(WaitingDao.class)
@Sql("/testReservationData.sql")
class WaitingDaoTest {

    // testReservationData.sql 기준 waiting:
    // id=1: user_d / 2026-04-28 / time_id=1(10:00) / theme_id=1
    // id=2: user_d / 2026-06-05 / time_id=2(12:00) / theme_id=1
    // id=3: user_e / 2026-06-05 / time_id=1(10:00) / theme_id=1
    // id=4: user_b / 2026-06-05 / time_id=1(10:00) / theme_id=1

    @Autowired
    private WaitingDao waitingDao;

    private Slot slot(LocalDate date, long timeId, LocalTime startAt, long themeId) {
        return new Slot(date, ReservationTime.create(timeId, startAt), Theme.create(themeId, "테마", "url", "설명"));
    }

    @Test
    @DisplayName("내 대기와 같은 슬롯의 모든 대기(타인 포함)를 반환한다.")
    void findAllSharingSlotWith_sameSlot() {
        List<Waiting> result = waitingDao.findAllSharingSlotWith(new Member("user_e"));

        assertThat(result)
                .extracting(Waiting::id)
                .containsExactlyInAnyOrder(3L, 4L);
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
        Slot slot = slot(LocalDate.of(2026, 6, 5), 1, LocalTime.of(10, 0), 1);

        assertThat(waitingDao.existsBySlotAndOwner(slot, new Member("user_e"))).isTrue();
    }

    @Test
    @DisplayName("해당 슬롯에 회원의 대기가 없으면 false를 반환한다.")
    void existsBySlotAndOwner_false() {
        Slot slot = slot(LocalDate.of(2026, 6, 5), 1, LocalTime.of(10, 0), 1);

        assertThat(waitingDao.existsBySlotAndOwner(slot, new Member("없는유저"))).isFalse();
    }
}
