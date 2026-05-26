package roomescape.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;

@SpringBootTest
@Transactional
class ReservationDaoTest {

    @Autowired
    private ReservationDao reservationDao;

    @Autowired
    private ReservationTimeDao reservationTimeDao;

    @Autowired
    private ThemeDao themeDao;

    @Test
    void 전체_예약_조회() {
        List<Reservation> reservations = reservationDao.findAll();
        assertThat(reservations).hasSize(19);
    }

    @Test
    void ID로_예약_조회() {
        Reservation reservation = reservationDao.findById(1L);
        assertThat(reservation).isNotNull();
        assertThat(reservation.getId()).isEqualTo(1L);
    }

    @Test
    void 이름으로_예약_조회() {
        Optional<Reservation> reservation = reservationDao.findByName("브라운");
        assertThat(reservation).isPresent();
        assertThat(reservation.get().getName()).isEqualTo("브라운");
    }

    @Test
    void 존재하지_않는_이름으로_조회하면_빈값_반환() {
        Optional<Reservation> reservation = reservationDao.findByName("없는이름");
        assertThat(reservation).isEmpty();
    }

    @Test
    void 예약_존재_여부_확인_존재하는_경우() {
        ReservationTime time = reservationTimeDao.findTimeById(1L);
        Theme theme = themeDao.findThemeById(1L);
        LocalDate date = LocalDate.now().minusDays(6);

        boolean exists = reservationDao.existsBy(date, theme, time);
        assertThat(exists).isTrue();
    }

    @Test
    void 예약_존재_여부_확인_존재하지_않는_경우() {
        ReservationTime time = reservationTimeDao.findTimeById(1L);
        Theme theme = themeDao.findThemeById(1L);
        LocalDate date = LocalDate.now().plusDays(10); // 미래 날짜

        boolean exists = reservationDao.existsBy(date, theme, time);
        assertThat(exists).isFalse();
    }

    @Test
    void 예약_저장() {
        ReservationTime time = reservationTimeDao.findTimeById(1L);
        Theme theme = themeDao.findThemeById(1L);
        Reservation reservation = new Reservation("테스트", LocalDate.now().plusDays(1), time, theme);

        Reservation saved = reservationDao.save(reservation);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("테스트");
    }

    @Test
    void 예약_날짜_시간_변경() {
        LocalDate newDate = LocalDate.now().plusDays(5);
        Long newTimeId = 2L;

        Reservation updated = reservationDao.update(1L, newDate, newTimeId);

        assertThat(updated.getDate()).isEqualTo(newDate);
        assertThat(updated.getTime().getId()).isEqualTo(newTimeId);
    }

    @Test
    void 예약_삭제() {
        reservationDao.delete(1L);

        assertThatThrownBy(() -> reservationDao.findById(1L))
                .isInstanceOf(EmptyResultDataAccessException.class);
    }
}
