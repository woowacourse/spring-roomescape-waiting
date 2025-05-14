package roomescape.dao.jdbc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import roomescape.entity.Member;
import roomescape.entity.Reservation;
import roomescape.entity.ReservationTime;
import roomescape.entity.Theme;
import roomescape.exception.custom.NotFoundException;

@JdbcTest
@Import(JdbcReservationDao.class)
class JdbcReservationDaoTest {

    @Autowired
    private JdbcReservationDao jdbcReservationDao;

    @Test
    @DisplayName("전체 예약 기록을 조회할 수 있다.")
    void findAllReservation() {
        List<Reservation> reservations = jdbcReservationDao.findAllReservations();

        assertThat(reservations).hasSize(3);
    }

    @Test
    @DisplayName("해당 날짜, 시간, 테마로 해당 예약이 있다면 true를 반환한다.")
    void existReservationByDateTimeAndTheme() {
        LocalDate date = LocalDate.of(2025, 4, 28);
        Long timeId = 1L;
        Long themeId = 1L;

        assertThat(jdbcReservationDao.existReservationByDateTimeAndTheme(date, timeId, themeId))
            .isTrue();
    }

    @Test
    @DisplayName("해당 날짜, 시간, 테마로 해당 예약이 없다면 false 반환한다.")
    void notExistReservationByDateTimeAndTheme() {
        LocalDate date = LocalDate.of(2025, 5, 28);
        Long timeId = 1L;
        Long themeId = 1L;

        assertThat(jdbcReservationDao.existReservationByDateTimeAndTheme(date, timeId, themeId))
            .isFalse();
    }

    @Test
    @DisplayName("예약을 추가할 수 있다.")
    void addReservation() {
        Member member = new Member(1L, "사나", "이메일", "비밀번호");
        ReservationTime time = new ReservationTime(1L, LocalTime.of(10, 0));
        Theme theme = new Theme(1L, "이름", "설명", "썸네일");
        Reservation reservation = new Reservation(member, LocalDate.of(2024, 4, 22), time, theme);

        Reservation newReservation = jdbcReservationDao.addReservation(reservation);

        assertThat(newReservation).isNotNull();
    }

    @Test
    @DisplayName("ID로 예약을 삭제할 수 있다.")
    void removeReservation() {
        Member member = new Member(1L, "사나", "이메일", "비밀번호");
        ReservationTime time = new ReservationTime(1L, LocalTime.of(10, 0));
        Theme theme = new Theme(1L, "이름", "설명", "썸네일");
        Reservation reservation = new Reservation(member, LocalDate.of(2024, 4, 22), time, theme);

        Reservation newReservation = jdbcReservationDao.addReservation(reservation);

        assertThatCode(() -> jdbcReservationDao.removeReservationById(newReservation.getId()))
            .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("해당 ID가 없다면 예약을 삭제할 수 없다.")
    void notRemoveReservation() {
        Member member = new Member(1L, "사나", "이메일", "비밀번호");
        ReservationTime time = new ReservationTime(1L, LocalTime.of(10, 0));
        Theme theme = new Theme(1L, "이름", "설명", "썸네일");
        Reservation reservation = new Reservation(member, LocalDate.of(2024, 4, 22), time, theme);

        Reservation newReservation = jdbcReservationDao.addReservation(reservation);
        jdbcReservationDao.removeReservationById(newReservation.getId());

        assertThatThrownBy(() -> jdbcReservationDao.removeReservationById(newReservation.getId()))
            .isInstanceOf(NotFoundException.class)
            .hasMessageContaining("reservation");
    }
}
