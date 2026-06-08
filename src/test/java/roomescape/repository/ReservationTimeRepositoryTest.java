package roomescape.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.EmptyResultDataAccessException;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;

@JdbcTest
@Import({ReservationTimeRepository.class, ReservationRepository.class, ThemeRepository.class})
class ReservationTimeRepositoryTest {

    @Autowired
    private ReservationTimeRepository reservationTimeDao;

    @Autowired
    private ReservationRepository reservationDao;

    @Autowired
    private ThemeRepository themeDao;

    @Test
    void 전체_시간_조회() {
        reservationTimeDao.save(new ReservationTime(LocalTime.of(10, 0)));
        reservationTimeDao.save(new ReservationTime(LocalTime.of(11, 0)));

        List<ReservationTime> times = reservationTimeDao.findAll();

        assertThat(times).hasSize(2);
    }

    @Test
    void ID로_시간_조회() {
        ReservationTime saved = reservationTimeDao.save(new ReservationTime(LocalTime.of(10, 0)));

        ReservationTime found = reservationTimeDao.findTimeById(saved.getId());

        assertThat(found.getId()).isEqualTo(saved.getId());
        assertThat(found.getStartAt()).isEqualTo(LocalTime.of(10, 0));
    }

    @Test
    void 시간_저장() {
        ReservationTime saved = reservationTimeDao.save(new ReservationTime(LocalTime.of(19, 0)));

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getStartAt()).isEqualTo(LocalTime.of(19, 0));
    }

    @Test
    void 예약에_사용중인_시간_존재하는_경우() {
        ReservationTime time = reservationTimeDao.save(new ReservationTime(LocalTime.of(10, 0)));
        Theme theme = themeDao.save(new Theme(null, "테마", "설명", "/url"));
        reservationDao.save(new Reservation("브라운", LocalDate.now().plusDays(1), time, theme, LocalDateTime.now()));

        assertThat(reservationTimeDao.existsByTimeId(time.getId())).isTrue();
    }

    @Test
    void 예약에_사용중인_시간_존재하지_않는_경우() {
        ReservationTime time = reservationTimeDao.save(new ReservationTime(LocalTime.of(10, 0)));

        assertThat(reservationTimeDao.existsByTimeId(time.getId())).isFalse();
    }

    @Test
    void 시간_삭제() {
        ReservationTime time = reservationTimeDao.save(new ReservationTime(LocalTime.of(10, 0)));

        reservationTimeDao.delete(time.getId());

        assertThatThrownBy(() -> reservationTimeDao.findTimeById(time.getId()))
                .isInstanceOf(EmptyResultDataAccessException.class);
    }
}
