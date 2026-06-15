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
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Slot;
import roomescape.domain.Theme;

@JdbcTest
@Import({ReservationTimeDao.class, ReservationDao.class, ThemeDao.class})
@Sql("/truncate.sql")
class ReservationTimeDaoTest {

    private static final LocalDate DATE = LocalDate.of(2026, 6, 5);

    @Autowired
    private ReservationTimeDao timeDao;
    @Autowired
    private ReservationDao reservationDao;
    @Autowired
    private ThemeDao themeDao;

    @Test
    @DisplayName("예약된 슬롯의 시간은 조회 결과에서 제외된다.")
    void findUnreservedBy_excludesReservedTime() {
        Theme theme = themeDao.save(Theme.create(0, "테마", "url", "설명"));
        ReservationTime bookedTime = timeDao.save(ReservationTime.create(0, LocalTime.of(10, 0)));
        ReservationTime freeTime = timeDao.save(ReservationTime.create(0, LocalTime.of(12, 0)));

        reservationDao.save(Reservation.forNew(new Member("user_a"), new Slot(DATE, bookedTime, theme)));

        List<ReservationTime> result = timeDao.findUnreservedBy(DATE, theme.id());

        assertThat(result).extracting(ReservationTime::id)
                .containsExactly(freeTime.id())
                .doesNotContain(bookedTime.id());
    }

    @Test
    @DisplayName("다른 날짜의 예약은 조회 결과를 제한하지 않는다.")
    void findUnreservedBy_differentDateDoesNotExclude() {
        Theme theme = themeDao.save(Theme.create(0, "테마", "url", "설명"));
        ReservationTime time = timeDao.save(ReservationTime.create(0, LocalTime.of(10, 0)));

        reservationDao.save(Reservation.forNew(new Member("user_a"), new Slot(DATE.plusDays(1), time, theme)));

        List<ReservationTime> result = timeDao.findUnreservedBy(DATE, theme.id());

        assertThat(result).extracting(ReservationTime::id)
                .contains(time.id());
    }

    @Test
    @DisplayName("예약이 없으면 모든 시간을 반환한다.")
    void findUnreservedBy_noReservations() {
        Theme theme = themeDao.save(Theme.create(0, "테마", "url", "설명"));
        ReservationTime time1 = timeDao.save(ReservationTime.create(0, LocalTime.of(10, 0)));
        ReservationTime time2 = timeDao.save(ReservationTime.create(0, LocalTime.of(12, 0)));

        List<ReservationTime> result = timeDao.findUnreservedBy(DATE, theme.id());

        assertThat(result).extracting(ReservationTime::id)
                .containsExactlyInAnyOrder(time1.id(), time2.id());
    }

    @Test
    @DisplayName("예약 가능한 시간은 시작 시간 오름차순으로 반환한다.")
    void findUnreservedBy_ordersByStartAt() {
        Theme theme = themeDao.save(Theme.create(0, "테마", "url", "설명"));
        timeDao.save(ReservationTime.create(0, LocalTime.of(14, 0)));
        timeDao.save(ReservationTime.create(0, LocalTime.of(10, 0)));

        List<ReservationTime> result = timeDao.findUnreservedBy(DATE, theme.id());

        assertThat(result).extracting(ReservationTime::startAt)
                .containsExactly(LocalTime.of(10, 0), LocalTime.of(14, 0));
    }
}
