package roomescape.repository;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@Import({JdbcReservationRepository.class, JdbcReservationTimeRepository.class, JdbcThemeRepository.class})
class JdbcReservationRepositoryTest {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ThemeRepository themeRepository;


    @Test
    void 예약을_저장하고_조회한다() {
        ReservationTime time = reservationTimeRepository.save(new ReservationTime(LocalTime.of(10, 0)));
        Theme theme = themeRepository.save(new Theme("레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.", "https://example.com/theme.png"));
        Reservation reservation = reservationRepository.save(new Reservation("브라운", LocalDate.of(2023, 8, 5), time, theme));

        List<Reservation> reservations = reservationRepository.findAll();

        assertThat(reservations).hasSize(1);

        Reservation found = reservations.getFirst();
        assertThat(found.getId()).isEqualTo(reservation.getId());
        assertThat(found.getName()).isEqualTo("브라운");
        assertThat(found.getDate()).isEqualTo(LocalDate.of(2023, 8, 5));

        assertThat(found.getTime().getId()).isEqualTo(time.getId());
        assertThat(found.getTime().getStartAt()).isEqualTo(LocalTime.of(10, 0));

        assertThat(found.getTheme().getId()).isEqualTo(theme.getId());
        assertThat(found.getTheme().getName()).isEqualTo(theme.getName());
        assertThat(found.getTheme().getDescription()).isEqualTo(theme.getDescription());
        assertThat(found.getTheme().getThumbnail()).isEqualTo(theme.getThumbnail());
    }

    @Test
    @DisplayName("특정 날짜, 시간, 테마를 가진 예약이 존재하는지 확인한다.")
    public void existsByDateAndTimeIdAndThemeId() {
        // given
        ReservationTime time = reservationTimeRepository.save(new ReservationTime(LocalTime.of(10, 0)));
        ReservationTime time2 = reservationTimeRepository.save(new ReservationTime(LocalTime.of(12, 0)));
        Theme theme = themeRepository.save(new Theme("레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.", "https://example.com/theme.png"));
        Theme theme2 = themeRepository.save(new Theme("레벨3 탈출", "우테코 레벨3을 탈출하는 내용입니다.", "https://example.com/theme.png"));
        LocalDate targetDate = LocalDate.of(2023, 8, 5);
        reservationRepository.save(new Reservation("브라운", targetDate, time, theme));

        // when
        boolean exists = reservationRepository.existsByDateAndTimeIdAndThemeId(targetDate, time.getId(), theme.getId());
        boolean notExists = reservationRepository.existsByDateAndTimeIdAndThemeId(targetDate, time2.getId(), theme2.getId());

        // then
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    @DisplayName("예약을 삭제한다.")
    void deleteById() {
        ReservationTime time = reservationTimeRepository.save(new ReservationTime(LocalTime.of(10, 0)));
        Theme theme = themeRepository.save(new Theme("레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.", "https://example.com/theme.png"));
        Reservation reservation = reservationRepository.save(new Reservation("브라운", LocalDate.of(2023, 8, 5), time, theme));

        boolean deleted = reservationRepository.deleteById(reservation.getId());

        assertThat(deleted).isTrue();
        assertThat(reservationRepository.findAll()).isEmpty();
    }

    @Test
    @DisplayName("존재하지 않는 예약은 삭제되지 않는다.")
    public void deleteById_fail() {
        // given
        Long id = 1L;

        // when
        boolean deleted = reservationRepository.deleteById(id);

        // then
        assertThat(deleted).isFalse();
    }

    @Test
    @DisplayName("특정 예약 시간 id를 가진 예약이 존재하는지 확인한다.")
    public void existByTimeId() {
        // given
        ReservationTime time = reservationTimeRepository.save(new ReservationTime(LocalTime.of(10, 0)));
        ReservationTime time2 = reservationTimeRepository.save(new ReservationTime(LocalTime.of(12, 0)));
        Theme theme = themeRepository.save(new Theme("레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.", "https://example.com/theme.png"));
        reservationRepository.save(new Reservation("브라운", LocalDate.of(2023, 8, 5), time, theme));

        // when
        boolean exists = reservationRepository.existByTimeId(time.getId());
        boolean notExists = reservationRepository.existByTimeId(time2.getId());

        // then
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    @DisplayName("특정 테마 id를 가진 예약이 존재하는지 확인한다.")
    public void existByThemeId() {
        // given
        ReservationTime time = reservationTimeRepository.save(new ReservationTime(LocalTime.of(10, 0)));
        Theme theme = themeRepository.save(new Theme("레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.", "https://example.com/theme.png"));
        Theme theme2 = themeRepository.save(new Theme("레벨3 탈출", "우테코 레벨3을 탈출하는 내용입니다.", "https://example.com/theme.png"));
        reservationRepository.save(new Reservation("브라운", LocalDate.of(2023, 8, 5), time, theme));

        // when
        boolean exists = reservationRepository.existByThemeId(theme.getId());
        boolean notExists = reservationRepository.existByThemeId(theme2.getId());

        // then
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }
}
