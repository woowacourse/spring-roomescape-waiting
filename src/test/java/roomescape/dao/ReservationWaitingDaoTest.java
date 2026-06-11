package roomescape.dao;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import roomescape.domain.ReservationSlot;
import roomescape.domain.ReservationWaiting;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@JdbcTest
@Import({ReservationWaitingDao.class, ReservationSlotDao.class, ReservationTimeDao.class, ThemeDao.class})
class ReservationWaitingDaoTest {

    @Autowired
    private ReservationWaitingDao reservationWaitingDao;

    @Autowired
    private ReservationTimeDao timeDao;

    @Autowired
    private ThemeDao themeDao;

    @Test
    void 예약_대기를_생성한다() {
        // given
        ReservationTime savedTime = saveTime(10, 0);
        Theme savedTheme = saveTheme("방탈출1", "설명", "https://asdfsdf.sdfs");
        ReservationWaiting reservationWaiting = ReservationWaiting.createWithoutId("맥스", LocalDateTime.now(), LocalDate.of(2026, 6, 10), savedTime, savedTheme);

        // when
        ReservationWaiting saved = reservationWaitingDao.insert(reservationWaiting);

        // then
        assertThat(saved.getSlot().getId()).isNotNull();

        assertThat(saved)
                .extracting(ReservationWaiting::getName, ReservationWaiting::getCreatedAt, ReservationWaiting::getReservationDate, ReservationWaiting::getTime, ReservationWaiting::getTheme)
                .containsExactly(reservationWaiting.getName(), reservationWaiting.getCreatedAt(), reservationWaiting.getReservationDate(), reservationWaiting.getTime(), reservationWaiting.getTheme());
    }

    @Test
    void 예약_대기를_삭제한다() {
        // given
        ReservationTime savedTime = saveTime(10, 0);
        Theme savedTheme = saveTheme("방탈출1", "설명", "https://asdfsdf.sdfs");

        ReservationWaiting reservationWaiting = ReservationWaiting.createWithoutId("맥스", LocalDateTime.now(), LocalDate.of(2026, 6, 10), savedTime, savedTheme);
        ReservationWaiting saved = reservationWaitingDao.insert(reservationWaiting);

        // when
        reservationWaitingDao.delete(saved.getId());

        // then
        assertThat(reservationWaitingDao.select()).isEmpty();
    }

    @Test
    void 특정_날짜_테마_시간_사용자_이름에_예약_대기가_존재하면_true를_반환한다() {
        // given
        ReservationTime savedTime = saveTime(10, 0);
        Theme savedTheme = saveTheme("방탈출1", "설명", "https://asdfsdf.sdfs");

        ReservationWaiting reservationWaiting = ReservationWaiting.createWithoutId("맥스", LocalDateTime.now(), LocalDate.of(2026, 6, 10), savedTime, savedTheme);
        reservationWaitingDao.insert(reservationWaiting);

        // when
        boolean result = reservationWaitingDao.existsByNameAndDateAndTimeIdAndThemeId(
                reservationWaiting.getName(),
                new ReservationSlot(reservationWaiting.getReservationDate(), savedTime, savedTheme)
        );

        // then
        assertThat(result).isTrue();
    }

    @Test
    void 특정_날짜_테마_시간_사용자_이름에_예약_대기가_존재하지_않으면_false를_반환한다() {
        ReservationTime notSavedTime = new ReservationTime(999L, LocalTime.of(10, 0));
        Theme notSavedTheme = new Theme(999L, "존재하지 않는 테마", "설명", "https://thumb.com");

        // when
        boolean result = reservationWaitingDao.existsByNameAndDateAndTimeIdAndThemeId(
                "맥스",
                new ReservationSlot(LocalDate.of(2026, 6, 10), notSavedTime, notSavedTheme)
        );

        // then
        assertThat(result).isFalse();
    }

    @Test
    void 전체_대기_목록을_조회한다() {
        // given
        ReservationTime savedTime = saveTime(10, 0);
        Theme savedTheme = saveTheme("방탈출1", "설명", "https://asdfsdf.sdfs");

        ReservationWaiting reservationWaiting1 = ReservationWaiting.createWithoutId("맥스", LocalDateTime.now(), LocalDate.of(2026, 6, 10), savedTime, savedTheme);
        ReservationWaiting reservationWaiting2 = ReservationWaiting.createWithoutId("로지", LocalDateTime.now(), LocalDate.of(2026, 6, 10), savedTime, savedTheme);
        ReservationWaiting savedReservationWaiting1 = reservationWaitingDao.insert(reservationWaiting1);
        ReservationWaiting savedReservationWaiting2 = reservationWaitingDao.insert(reservationWaiting2);

        // when
        List<ReservationWaiting> reservationWaitings = reservationWaitingDao.select();

        // then
        assertAll(
                () -> assertThat(reservationWaitings.size()).isEqualTo(2),
                () -> assertThat(reservationWaitings.getFirst().getName()).isEqualTo("맥스")
        );
    }

    @Test
    void 특정_슬롯의_예약_대기_목록을_조회한다() {
        // given
        ReservationTime time = saveTime(10, 0);
        ReservationTime otherTime = saveTime(11, 0);
        Theme theme = saveTheme("방탈출1", "설명", "https://thumb.com");
        LocalDate date = LocalDate.of(2026, 6, 10);

        ReservationWaiting waiting1 = reservationWaitingDao.insert(
                ReservationWaiting.createWithoutId("브라운", LocalDateTime.now(), date, time, theme)
        );
        ReservationWaiting waiting2 = reservationWaitingDao.insert(
                ReservationWaiting.createWithoutId("로지", LocalDateTime.now(), date, time, theme)
        );
        reservationWaitingDao.insert(
                ReservationWaiting.createWithoutId("맥스", LocalDateTime.now(), date, otherTime, theme)
        );

        ReservationSlot slot = new ReservationSlot(date, time, theme);

        // when
        List<ReservationWaiting> result = reservationWaitingDao.selectBySlot(slot);

        // then
        assertThat(result).containsExactly(waiting1, waiting2);
    }

    @Test
    void 슬롯_아이디로_예약_대기_목록을_조회한다() {
        // given
        ReservationTime time = saveTime(10, 0);
        ReservationTime otherTime = saveTime(11, 0);
        Theme theme = saveTheme("방탈출1", "설명", "https://thumb.com");
        LocalDate date = LocalDate.of(2026, 6, 10);

        ReservationWaiting waiting1 = reservationWaitingDao.insert(
                ReservationWaiting.createWithoutId("브라운", LocalDateTime.now(), date, time, theme)
        );
        ReservationWaiting waiting2 = reservationWaitingDao.insert(
                ReservationWaiting.createWithoutId("로지", LocalDateTime.now(), date, time, theme)
        );
        reservationWaitingDao.insert(
                ReservationWaiting.createWithoutId("맥스", LocalDateTime.now(), date, otherTime, theme)
        );

        Long slotId = waiting1.getSlot().getId();

        // when
        List<ReservationWaiting> result = reservationWaitingDao.selectBySlotId(slotId);

        // then
        assertThat(result).containsExactly(waiting1, waiting2);
    }

    @Test
    void 이름과_슬롯_아이디에_해당하는_예약_대기가_존재하면_true를_반환한다() {
        ReservationTime time = saveTime(10, 0);
        Theme theme = saveTheme("방탈출1", "설명", "https://thumb.com");

        ReservationWaiting waiting = reservationWaitingDao.insert(
                ReservationWaiting.createWithoutId(
                        "맥스",
                        LocalDateTime.now(),
                        LocalDate.of(2026, 6, 10),
                        time,
                        theme
                )
        );

        boolean result = reservationWaitingDao.existsByNameAndSlotId("맥스", waiting.getSlot().getId());

        assertThat(result).isTrue();
    }

    @Test
    void 이름과_슬롯_아이디에_해당하는_예약_대기가_없으면_false를_반환한다() {
        boolean result = reservationWaitingDao.existsByNameAndSlotId("맥스", 999L);

        assertThat(result).isFalse();
    }

    private ReservationTime saveTime(int hour, int minute) {
        return timeDao.insert(ReservationTime.createWithoutId(LocalTime.of(hour, minute)));
    }

    private Theme saveTheme(String name, String description, String thumbnail) {
        return themeDao.insert(Theme.createWithoutId(name, description, thumbnail));
    }
}
