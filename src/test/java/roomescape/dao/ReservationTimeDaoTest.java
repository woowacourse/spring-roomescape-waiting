package roomescape.dao;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;

@JdbcTest
@Import({ReservationTimeDao.class, ReservationDao.class, ThemeDao.class})
class ReservationTimeDaoTest {

    @Autowired
    private ReservationTimeDao reservationTimeDao;

    @Autowired
    private ReservationDao reservationDao;

    @Autowired
    private ThemeDao themeDao;

    @Test
    void ID로_시간_조회() {
        ReservationTime saved = reservationTimeDao.save(new ReservationTime(LocalTime.of(9, 0)));
        Optional<ReservationTime> time = reservationTimeDao.findTimeById(saved.getId());

        assertThat(time)
                .map(ReservationTime::getId)
                .hasValue(saved.getId());

        assertThat(time)
                .map(ReservationTime::getStartAt)
                .hasValue(LocalTime.of(9, 0));
    }

    @Test
    void 시간_저장() {
        ReservationTime newTime = new ReservationTime(LocalTime.of(19, 0));

        ReservationTime saved = reservationTimeDao.save(newTime);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getStartAt()).isEqualTo(LocalTime.of(19, 0));
    }

    @Test
    void 예약에_사용중인_시간_존재하는_경우() {
        ReservationTime time = reservationTimeDao.save(new ReservationTime(LocalTime.of(9, 0)));
        Theme theme = themeDao.save(new Theme("테마", "설명", "url"));
        reservationDao.save(new Reservation("브라운", LocalDate.now(), time, theme, ReservationStatus.CONFIRMED));

        boolean exists = reservationTimeDao.existsByTimeId(time.getId());

        assertThat(exists).isTrue();
    }

    @Test
    void 예약에_사용중인_시간_존재하지_않는_경우() {
        ReservationTime time = reservationTimeDao.save(new ReservationTime(LocalTime.of(9, 0)));
        
        boolean exists = reservationTimeDao.existsByTimeId(time.getId());

        assertThat(exists).isFalse();
    }

    @Test
    void 시간_삭제() {
        ReservationTime time = reservationTimeDao.save(new ReservationTime(LocalTime.of(9, 0)));
        
        reservationTimeDao.delete(time.getId());

        assertThat(reservationTimeDao.findTimeById(time.getId())).isEmpty();
    }
}
