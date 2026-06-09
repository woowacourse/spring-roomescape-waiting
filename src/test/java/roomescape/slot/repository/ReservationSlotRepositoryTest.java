package roomescape.slot.repository;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import roomescape.date.domain.ReservationDate;
import roomescape.date.fixture.ReservationDateFixture;
import roomescape.date.repository.JdbcReservationDateRepository;
import roomescape.slot.domain.ReservationSlot;
import roomescape.theme.domain.Theme;
import roomescape.theme.fixture.ThemeFixture;
import roomescape.theme.repository.JdbcThemeRepository;
import roomescape.time.domain.ReservationTime;
import roomescape.time.fixture.ReservationTimeFixture;
import roomescape.time.repository.JdbcReservationTimeRepository;

import java.util.Optional;

@JdbcTest
@Import({
        JdbcReservationSlotRepository.class,
        JdbcReservationTimeRepository.class,
        JdbcReservationDateRepository.class,
        JdbcThemeRepository.class
})
class ReservationSlotRepositoryTest {

    @Autowired
    private JdbcReservationTimeRepository reservationTimeRepository;

    @Autowired
    private JdbcReservationDateRepository reservationDateRepository;

    @Autowired
    private JdbcThemeRepository themeRepository;

    @Autowired
    private JdbcReservationSlotRepository reservationSlotRepository;

    @Test
    @DisplayName("등록된 슬롯의 테마가 비활성화라면 유효하지 않다.")
    void readSlot_inactiveTheme() {
        // given
        ReservationDate date = reservationDateRepository.save(ReservationDateFixture.activeOneWeekLater());
        ReservationTime time = reservationTimeRepository.save(ReservationTimeFixture.activeTime15());
        Theme theme = themeRepository.save(ThemeFixture.activeTheme());
        reservationSlotRepository.save(ReservationSlot.of(date, time, theme));

        theme.updateStatus(false);
        themeRepository.updateStatus(theme);

        // when
        Optional<ReservationSlot> actual = reservationSlotRepository.findAvailableByDateIdTimeIdThemeId(date.getId(), time.getId(), theme.getId());

        // then
        Assertions.assertThat(actual)
                .isEmpty();
    }

    @Test
    @DisplayName("등록된 슬롯의 테마가 비활성화라면 유효하지 않다.")
    void findSlot_inactiveTheme() {
        // given
        ReservationDate date = reservationDateRepository.save(ReservationDateFixture.activeOneWeekLater());
        ReservationTime time = reservationTimeRepository.save(ReservationTimeFixture.activeTime15());
        Theme theme = themeRepository.save(ThemeFixture.activeTheme());
        reservationSlotRepository.save(ReservationSlot.of(date, time, theme));

        theme.updateStatus(false);
        themeRepository.updateStatus(theme);

        // when
        Optional<ReservationSlot> actual = reservationSlotRepository.findAvailableByDateIdTimeIdThemeId(date.getId(), time.getId(), theme.getId());

        // then
        Assertions.assertThat(actual)
                .isEmpty();
    }

    @Test
    @DisplayName("등록된 슬롯의 날짜가 비활성화라면 유효하지 않다.")
    void findSlot_inactiveDate() {
        // given
        ReservationDate date = reservationDateRepository.save(ReservationDateFixture.activeOneWeekLater());
        ReservationTime time = reservationTimeRepository.save(ReservationTimeFixture.activeTime15());
        Theme theme = themeRepository.save(ThemeFixture.activeTheme());
        reservationSlotRepository.save(ReservationSlot.of(date, time, theme));

        date.updateStatus(false);
        reservationDateRepository.updateStatus(date);

        // when
        Optional<ReservationSlot> actual = reservationSlotRepository.findAvailableByDateIdTimeIdThemeId(date.getId(), time.getId(), theme.getId());

        // then
        Assertions.assertThat(actual)
                .isEmpty();
    }

    @Test
    @DisplayName("등록된 슬롯의 시간이 비활성화라면 유효하지 않다.")
    void findSlot_inactiveTime() {
        // given
        ReservationDate date = reservationDateRepository.save(ReservationDateFixture.activeOneWeekLater());
        ReservationTime time = reservationTimeRepository.save(ReservationTimeFixture.activeTime15());
        Theme theme = themeRepository.save(ThemeFixture.activeTheme());
        reservationSlotRepository.save(ReservationSlot.of(date, time, theme));

        time.updateStatus(false);
        reservationTimeRepository.updateStatus(time);

        // when
        Optional<ReservationSlot> actual = reservationSlotRepository.findAvailableByDateIdTimeIdThemeId(date.getId(), time.getId(), theme.getId());

        // then
        Assertions.assertThat(actual)
                .isEmpty();
    }

}
