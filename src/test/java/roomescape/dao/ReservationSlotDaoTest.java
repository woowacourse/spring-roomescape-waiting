package roomescape.dao;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import roomescape.domain.ReservationSlot;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@JdbcTest
@Import({ReservationSlotDao.class, ReservationTimeDao.class, ThemeDao.class})
public class ReservationSlotDaoTest {

    @Autowired
    private ReservationSlotDao reservationSlotDao;

    @Autowired
    private ReservationTimeDao reservationTimeDao;

    @Autowired
    private ThemeDao themeDao;

    @Test
    void 예약_슬롯을_생성한다() {
        ReservationTime time = saveTime(10, 0);
        Theme theme = saveTheme("방탈출1", "설명", "https://thumb.com");
        ReservationSlot slot = new ReservationSlot(LocalDate.of(2026, 6, 10), time, theme);

        ReservationSlot saved = reservationSlotDao.insert(slot);

        assertAll(
                () -> assertThat(saved.getId()).isNotNull(),
                () -> assertThat(saved.getDate()).isEqualTo(slot.getDate()),
                () -> assertThat(saved.getTime()).isEqualTo(time),
                () -> assertThat(saved.getTheme()).isEqualTo(theme)
        );
    }

    @Test
    void 아이디로_예약_슬롯을_조회한다() {
        ReservationTime time = saveTime(10, 0);
        Theme theme = saveTheme("방탈출1", "설명", "https://thumb.com");
        ReservationSlot saved = reservationSlotDao.insert(
                new ReservationSlot(LocalDate.of(2026, 6, 10), time, theme)
        );

        Optional<ReservationSlot> found = reservationSlotDao.selectById(saved.getId());

        assertThat(found).contains(saved);
    }

    @Test
    void 날짜_시간_테마로_예약_슬롯을_조회한다() {
        ReservationTime time = saveTime(10, 0);
        Theme theme = saveTheme("방탈출1", "설명", "https://thumb.com");
        ReservationSlot slot = new ReservationSlot(LocalDate.of(2026, 6, 10), time, theme);
        ReservationSlot saved = reservationSlotDao.insert(slot);

        Optional<ReservationSlot> found = reservationSlotDao.selectByDateAndTimeIdAndThemeId(slot);

        assertThat(found).contains(saved);
    }

    @Test
    void 존재하지_않는_예약_슬롯을_조회하면_빈_값을_반환한다() {
        Optional<ReservationSlot> found = reservationSlotDao.selectById(999L);

        assertThat(found).isEmpty();
    }

    @Test
    void 예약_슬롯이_없으면_생성한다() {
        ReservationTime time = saveTime(10, 0);
        Theme theme = saveTheme("방탈출1", "설명", "https://thumb.com");
        ReservationSlot slot = new ReservationSlot(LocalDate.of(2026, 6, 10), time, theme);

        ReservationSlot saved = reservationSlotDao.findOrCreate(slot);

        assertThat(saved.getId()).isNotNull();
        assertThat(reservationSlotDao.selectById(saved.getId())).contains(saved);
    }

    @Test
    void 예약_슬롯이_있으면_기존_슬롯을_반환한다() {
        ReservationTime time = saveTime(10, 0);
        Theme theme = saveTheme("방탈출1", "설명", "https://thumb.com");
        ReservationSlot slot = new ReservationSlot(LocalDate.of(2026, 6, 10), time, theme);

        ReservationSlot saved = reservationSlotDao.insert(slot);
        ReservationSlot found = reservationSlotDao.findOrCreate(slot);

        assertThat(found).isEqualTo(saved);

    }

    private ReservationTime saveTime(int hour, int minute) {
        return reservationTimeDao.insert(ReservationTime.createWithoutId(LocalTime.of(hour, minute)));
    }

    private Theme saveTheme(String name, String description, String thumbnail) {
        return themeDao.insert(Theme.createWithoutId(name, description, thumbnail));
    }
}
