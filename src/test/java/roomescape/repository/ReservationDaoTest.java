package roomescape.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Slot;
import roomescape.domain.Theme;

@JdbcTest
@Import({ReservationDao.class, ThemeDao.class, ReservationTimeDao.class})
@Transactional
class ReservationDaoTest {

    private static final LocalDate DATE = LocalDate.of(2026, 6, 5);

    @Autowired
    private ReservationDao reservationDao;
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
    @DisplayName("슬롯으로 예약을 조회한다.")
    void findBySlot_found() {
        Reservation saved = reservationDao.save(Reservation.forNew(new Member("user_a"), slotA));

        Optional<Reservation> result = reservationDao.findBySlot(slotA);

        assertThat(result).isPresent();
        assertThat(result.get().id()).isEqualTo(saved.id());
        assertThat(result.get().owner()).isEqualTo(new Member("user_a"));
    }

    @Test
    @DisplayName("예약이 없는 슬롯은 빈 Optional을 반환한다.")
    void findBySlot_notFound() {
        Optional<Reservation> result = reservationDao.findBySlot(slotA);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("예약이 있는 슬롯은 true를 반환한다.")
    void existsBySlot_true() {
        reservationDao.save(Reservation.forNew(new Member("user_a"), slotA));

        assertThat(reservationDao.existsBySlot(slotA)).isTrue();
    }

    @Test
    @DisplayName("예약이 없는 슬롯은 false를 반환한다.")
    void existsBySlot_false() {
        assertThat(reservationDao.existsBySlot(slotA)).isFalse();
    }

    @Test
    @DisplayName("이름으로 조회하면 해당 회원의 예약만 반환한다.")
    void findAllByName_returnsOnlyMatching() {
        Reservation mine = reservationDao.save(Reservation.forNew(new Member("user_a"), slotA));
        reservationDao.save(Reservation.forNew(new Member("user_b"), slotB));

        List<Reservation> result = reservationDao.findAllByName(new Member("user_a"));

        assertThat(result).extracting(Reservation::id)
                .containsExactly(mine.id());
    }

    @Test
    @DisplayName("이름에 해당하는 예약이 없으면 빈 목록을 반환한다.")
    void findAllByName_noMatch() {
        List<Reservation> result = reservationDao.findAllByName(new Member("없는유저"));

        assertThat(result).isEmpty();
    }
}
