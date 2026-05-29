package roomescape.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static roomescape.config.FixedClockConfig.FUTURE_DATE;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.UserName;
import roomescape.domain.reservation.theme.Description;
import roomescape.domain.reservation.theme.Theme;
import roomescape.domain.reservation.theme.ThemeName;
import roomescape.domain.reservation.theme.ThumbnailUrl;
import roomescape.domain.reservation.time.ReservationTime;

@JdbcTest
@Import(ReservationDao.class)
class ReservationDaoTest {

    private final UserName userName = UserName.parse("토리");
    private final LocalDate futureDate = LocalDate.parse(FUTURE_DATE);
    private final ReservationTime unusedTime = new ReservationTime(9L, LocalTime.of(18, 0));
    private final Theme unusedTheme = themeWithId(15L);
    private final int reservedSize = 25;

    @Autowired
    private ReservationDao reservationDao;

    @Test
    @DisplayName("예약을 저장할 수 있다")
    void saveReservation() {
        Reservation reservation = new Reservation(userName, futureDate, unusedTime, unusedTheme);

        Reservation saved = reservationDao.save(reservation);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo(userName);
        assertThat(saved.getDate()).isEqualTo(futureDate);
        assertThat(saved.getTime()).isEqualTo(unusedTime);
        assertThat(saved.getTheme()).isEqualTo(unusedTheme);
    }

    @Test
    @DisplayName("저장된 모든 예약을 조회할 수 있다.")
    void findAllReservations() {
        List<Reservation> all = reservationDao.findAll();

        assertThat(all).hasSize(reservedSize);
    }

    @Test
    @DisplayName("사용자 이름으로 저장된 예약들을 조회할 수 있다.")
    void findReservationByUserName() {
        List<Reservation> reservations = reservationDao.findAllByUserName("브라운");

        assertThat(reservations).hasSize(reservedSize - 5);
    }

    @Test
    @DisplayName("예약 번호를 통해서 예약을 조회할 수 있다.")
    void findReservationById() {
        Optional<Reservation> found = reservationDao.findById(1L);

        assertThat(found).isPresent();
        assertThat(found.get().getName().value()).isEqualTo("브라운");
        assertThat(found.get().getDate()).isEqualTo(LocalDate.of(2026, 5, 1));
    }

    @Test
    @DisplayName("같은 슬롯에 예약이 존재하면 True를 반환한다")
    void existsByThenResultTrue() {
        LocalDate date = LocalDate.of(2026, 5, 1);

        boolean result = reservationDao.existsBy(date, themeWithId(11L), timeWithId(1L));

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("같은 슬롯에 예약이 존재하지 않는다면 False를 반환한다.")
    void existsByThenResultFalse() {
        boolean result = reservationDao.existsBy(futureDate, unusedTheme, unusedTime);

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("존재하는 아이디에 대해 existsById는 true를 반환한다.")
    void existsByIdResultTrue() {
        assertThat(reservationDao.existsById(1L)).isTrue();
    }

    @Test
    @DisplayName("존재하지 않는 아이디에 대해 existsById는 false를 반환한다.")
    void existsByIdResultFalse() {
        assertThat(reservationDao.existsById(9999L)).isFalse();
    }

    @Test
    @DisplayName("동일한 사용자가 같은 슬롯에 예약을 한 경우 True를 반환한다.")
    void existsByUserNameAndSlotResultTrue() {
        LocalDate date = LocalDate.of(2026, 5, 1);

        boolean result = reservationDao.existsByUserNameAndSlot(
                "브라운", date, themeWithId(11L), timeWithId(1L));

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("다른 사용자의 슬롯에 대해서는 False를 반환한다.")
    void existsByUserNameAndSlotResultFalse() {
        LocalDate date = LocalDate.of(2026, 5, 1);

        boolean result = reservationDao.existsByUserNameAndSlot(
                "토리", date, themeWithId(11L), timeWithId(1L));

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("해당 시간을 사용하는 예약이 있으면 existsByTimeId는 true를 반환한다.")
    void existsByTimeIdResultTrue() {
        assertThat(reservationDao.existsByTimeId(1L)).isTrue();
    }

    @Test
    @DisplayName("해당 시간을 사용하는 예약이 없으면 existsByTimeId는 false를 반환한다.")
    void existsByTimeIdResultFalse() {
        assertThat(reservationDao.existsByTimeId(9L)).isFalse();
    }

    @Test
    @DisplayName("해당 테마를 사용하는 예약이 있으면 existsByThemeId는 true를 반환한다.")
    void existsByThemeIdResultTrue() {
        assertThat(reservationDao.existsByThemeId(11L)).isTrue();
    }

    @Test
    @DisplayName("해당 테마를 사용하는 예약이 없으면 existsByThemeId는 false를 반환한다.")
    void existsByThemeIdResultFalse() {
        assertThat(reservationDao.existsByThemeId(15L)).isFalse();
    }

    @Test
    @DisplayName("예약을 수정할 수 있다.")
    void updateReservation() {
        Reservation updated = new Reservation(
                1L, UserName.parse("아나키"), futureDate, unusedTime, unusedTheme);

        boolean result = reservationDao.update(updated);

        assertThat(result).isTrue();
        Reservation found = reservationDao.findById(1L).orElseThrow();
        assertThat(found.getName().value()).isEqualTo("아나키");
        assertThat(found.getDate()).isEqualTo(futureDate);
    }

    @Test
    @DisplayName("존재하지 않는 예약을 수정하면 false를 반환한다.")
    void updateResultFalseWhenNotExists() {
        Reservation updated = new Reservation(
                9999L, userName, futureDate, unusedTime, unusedTheme);

        assertThat(reservationDao.update(updated)).isFalse();
    }

    @Test
    @DisplayName("예약을 삭제할 수 있다.")
    void deleteReservation() {
        reservationDao.delete(1L);

        assertThat(reservationDao.existsById(1L)).isFalse();
    }

    private Theme themeWithId(Long id) {
        return new Theme(
                id,
                ThemeName.parse("dummy"),
                Description.parse("dummy"),
                ThumbnailUrl.parse("/images/dummy"));
    }

    private ReservationTime timeWithId(Long id) {
        return new ReservationTime(id, LocalTime.of(0, 0));
    }
}