package roomescape.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationSlot;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;

@JdbcTest
@Import({ReservationRepository.class, ReservationTimeRepository.class, ThemeRepository.class})
class ReservationRepositoryTest {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ReservationTimeRepository timeDao;

    @Autowired
    private ThemeRepository themeRepository;

    @Test
    void 예약을_생성한다() {
        // given
        ReservationTime savedTime = saveTime(10, 0);
        Theme savedTheme = saveTheme("방탈출1", "설명", "https://asdfsdf.sdfs");
        Reservation reservation = Reservation.createWithoutId("브라운",
                new ReservationSlot(LocalDate.of(2026, 5, 5), savedTime, savedTheme));

        // when
        Reservation saved = reservationRepository.insert(reservation);

        // then
        assertThat(saved)
                .extracting(Reservation::getId, Reservation::getName, Reservation::getDate, Reservation::getTime,
                        Reservation::getTheme)
                .containsExactly(saved.getId(), reservation.getName(), reservation.getDate(), reservation.getTime(),
                        reservation.getTheme());
    }

    @Test
    void 예약_목록을_조회한다() {
        // given
        ReservationTime savedTime1 = saveTime(10, 0);
        ReservationTime savedTime2 = saveTime(11, 0);
        ReservationTime savedTime3 = saveTime(12, 0);
        ReservationTime savedTime4 = saveTime(13, 0);
        ReservationTime savedTime5 = saveTime(14, 0);
        Theme savedTheme = saveTheme("방탈출1", "설명", "https://asdfsdf.sdfs");
        LocalDate date = LocalDate.of(2026, 5, 5);

        reservationRepository.insert(Reservation.createWithoutId("브라운", new ReservationSlot(date, savedTime1, savedTheme)));
        reservationRepository.insert(Reservation.createWithoutId("로지", new ReservationSlot(date, savedTime2, savedTheme)));
        reservationRepository.insert(Reservation.createWithoutId("러키", new ReservationSlot(date, savedTime3, savedTheme)));
        reservationRepository.insert(Reservation.createWithoutId("러로", new ReservationSlot(date, savedTime4, savedTheme)));
        reservationRepository.insert(Reservation.createWithoutId("밤밤", new ReservationSlot(date, savedTime5, savedTheme)));

        // when
        List<Reservation> reservations = reservationRepository.select();

        // then
        assertAll(
                () -> assertThat(reservations).hasSize(5),
                () -> assertThat(reservations.getFirst().getName()).isEqualTo("브라운")
        );
    }

    @Test
    void 특정_시간에_예약이_존재하면_true를_반환한다() {
        // given
        ReservationTime time = saveTime(10, 0);
        Theme theme = saveTheme("방탈출1", "설명", "https://thumb.com");
        reservationRepository.insert(Reservation.createWithoutId("브라운",
                new ReservationSlot(LocalDate.of(2026, 5, 5), time, theme)));

        // when
        boolean result = reservationRepository.existsByTimeId(time.getId());

        // then
        assertThat(result).isTrue();
    }

    @Test
    void 특정_시간에_예약이_존재하지_않으면_false를_반환한다() {
        // when
        boolean result = reservationRepository.existsByTimeId(999L);

        // then
        assertThat(result).isFalse();
    }

    @Test
    void 특정_테마에_예약이_존재하면_true를_반환한다() {
        // given
        ReservationTime time = saveTime(10, 0);
        Theme theme = saveTheme("방탈출1", "설명", "https://thumb.com");
        reservationRepository.insert(Reservation.createWithoutId("브라운",
                new ReservationSlot(LocalDate.of(2026, 5, 5), time, theme)));

        // when
        boolean result = reservationRepository.existsByThemeId(theme.getId());

        // then
        assertThat(result).isTrue();
    }

    @Test
    void 특정_테마에_예약이_존재하지_않으면_false를_반환한다() {
        // when
        boolean result = reservationRepository.existsByThemeId(999L);

        // then
        assertThat(result).isFalse();
    }

    @Test
    void 테마_아이디와_선택_날짜에_해당하는_예약_목록을_조회한다() {
        // given
        ReservationTime savedTime = saveTime(10, 0);
        Theme theme1 = saveTheme("방탈출1", "설명1", "https://asdfsdf.sdfs");
        Theme theme2 = saveTheme("방탈출2", "설명2", "https://asdfsdf.sdfs");
        LocalDate date = LocalDate.of(2026, 5, 5);

        reservationRepository.insert(Reservation.createWithoutId("러키", new ReservationSlot(date, savedTime, theme1)));
        reservationRepository.insert(Reservation.createWithoutId("로지", new ReservationSlot(date, savedTime, theme2)));

        // when
        List<Reservation> result = reservationRepository.selectByThemeIdAndDate(theme1.getId(), date);

        // then
        assertAll(
                () -> assertThat(result).hasSize(1),
                () -> assertThat(result.getFirst().getName()).isEqualTo("러키")
        );
    }

    @Test
    void 날짜_시간_테마가_모두_같은_예약이_존재하면_true를_반환한다() {
        // given
        ReservationTime time = saveTime(10, 0);
        Theme theme = saveTheme("방탈출1", "설명", "https://thumb.com");
        LocalDate date = LocalDate.of(2026, 5, 5);
        reservationRepository.insert(Reservation.createWithoutId("브라운", new ReservationSlot(date, time, theme)));

        // when
        boolean result = reservationRepository.existsBySlot(new ReservationSlot(date, time, theme));

        // then
        assertThat(result).isTrue();
    }

    @Test
    void 날짜_시간_테마가_모두_같은_예약이_없으면_false를_반환한다() {
        // given
        ReservationTime time = saveTime(10, 0);
        Theme theme = saveTheme("방탈출1", "설명", "https://thumb.com");
        LocalDate date = LocalDate.of(2026, 5, 5);

        // when
        boolean result = reservationRepository.existsBySlot(new ReservationSlot(date, time, theme));

        // then
        assertThat(result).isFalse();
    }

    @Test
    void 날짜_시간_테마_예약자가_모두_같은_예약이_존재하면_true를_반환한다() {
        // given
        ReservationTime time = saveTime(10, 0);
        Theme theme = saveTheme("방탈출1", "설명", "https://thumb.com");
        LocalDate date = LocalDate.of(2026, 5, 5);
        reservationRepository.insert(Reservation.createWithoutId("브라운", new ReservationSlot(date, time, theme)));

        // when
        boolean result = reservationRepository.existsByNameAndSlot("브라운", new ReservationSlot(date, time, theme));

        // then
        assertThat(result).isTrue();
    }

    @Test
    void 날짜_시간_테마_예약자가_모두_같은_예약이_없으면_false를_반환한다() {
        // given
        ReservationTime time = saveTime(10, 0);
        Theme theme = saveTheme("방탈출1", "설명", "https://thumb.com");
        LocalDate date = LocalDate.of(2026, 5, 5);

        // when
        boolean result = reservationRepository.existsByNameAndSlot("로지", new ReservationSlot(date, time, theme));

        // then
        assertThat(result).isFalse();
    }

    @Test
    void 예약을_수정한다() {
        // given
        ReservationTime time1 = saveTime(10, 0);
        ReservationTime time2 = saveTime(11, 0);
        Theme theme = saveTheme("방탈출1", "설명", "https://thumb.com");
        Reservation saved = reservationRepository.insert(
                Reservation.createWithoutId("브라운", new ReservationSlot(LocalDate.of(2026, 5, 5), time1, theme)));

        // when
        Reservation updated = reservationRepository.update(saved.getId(), LocalDate.of(2026, 5, 6), time2.getId());

        // then
        assertAll(
                () -> assertThat(updated.getDate()).isEqualTo(LocalDate.of(2026, 5, 6)),
                () -> assertThat(updated.getTime().getId()).isEqualTo(time2.getId())
        );
    }

    @Test
    void 예약을_삭제한다() {
        // given
        ReservationTime savedTime = saveTime(10, 0);
        Theme savedTheme = saveTheme("방탈출1", "설명", "https://asdfsdf.sdfs");
        Reservation saved = reservationRepository.insert(
                Reservation.createWithoutId("예약1", new ReservationSlot(LocalDate.of(2026, 5, 5), savedTime, savedTheme)));

        // when
        reservationRepository.delete(saved.getId());

        // then
        assertThat(reservationRepository.select()).isEmpty();
    }

    private ReservationTime saveTime(int hour, int minute) {
        return timeDao.insert(ReservationTime.createWithoutId(LocalTime.of(hour, minute)));
    }

    private Theme saveTheme(String name, String description, String thumbnail) {
        return themeRepository.insert(Theme.createWithoutId(name, description, thumbnail));
    }
}
