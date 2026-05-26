package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.domain.Theme;
import roomescape.domain.ThemeSlot;
import roomescape.domain.Time;
import roomescape.global.exception.CustomException;
import roomescape.repository.FakeReservationDao;
import roomescape.repository.FakeThemeDao;
import roomescape.repository.FakeThemeSlotDao;
import roomescape.repository.FakeTimeDao;
import roomescape.service.TimeService;

class TimeServiceTest {

    private TimeService reservationTimeService;
    private FakeTimeDao fakeTimeDao;
    private FakeThemeDao fakeThemeDao;
    private FakeThemeSlotDao fakeThemeSlotDao;
    private FakeReservationDao fakeReservationDao;

    @BeforeEach
    void setUp() {
        fakeTimeDao = new FakeTimeDao();
        fakeThemeDao = new FakeThemeDao();
        fakeThemeSlotDao = new FakeThemeSlotDao();
        fakeReservationDao = new FakeReservationDao();

        reservationTimeService = new TimeService(fakeTimeDao, fakeThemeSlotDao, fakeThemeDao, fakeReservationDao);
    }

    @Test
    @DisplayName("시간 정보를 입력하여 새로운 예약 시간을 생성하고 반환한다.")
    void saveTime() {
        Time time = reservationTimeService.saveTime(LocalTime.of(10, 0));
        assertThat(time.getStartAt()).isEqualTo(LocalTime.of(10, 0));
    }

    @Test
    @DisplayName("존재하는 예약 시간을 삭제하면 전체 목록에서 사라진다.")
    void removeTime() {
        Time time = reservationTimeService.saveTime(LocalTime.of(10, 0));
        reservationTimeService.removeTime(time.getId());
        assertThat(reservationTimeService.allTimes()).isEmpty();
    }

    @Test
    @DisplayName("모든 예약 시간 목록을 조회하여 반환한다.")
    void allTimes() {
        reservationTimeService.saveTime(LocalTime.of(10, 0));
        List<Time> times = reservationTimeService.allTimes();
        assertThat(times).hasSize(1);
    }

    @Test
    @DisplayName("식별자를 통해 특정 예약 시간 객체를 조회한다.")
    void findTime() {
        Time savedTime = reservationTimeService.saveTime(LocalTime.of(10, 0));
        Time foundTime = reservationTimeService.findTime(savedTime.getId());
        assertThat(foundTime.getStartAt()).isEqualTo(LocalTime.of(10, 0));
    }

    @Test
    @DisplayName("해당 테마, 날짜에 대한 슬롯이 없으면 등록된 모든 시간으로 슬롯을 생성하여 반환한다.")
    void findThemeSlotBy_createsSlots_whenNotExists() {
        Theme theme = fakeThemeDao.save(new Theme("테마1", "설명", "test.com"));
        fakeTimeDao.save(Time.of(LocalTime.of(10, 0)));
        fakeTimeDao.save(Time.of(LocalTime.of(14, 0)));
        LocalDate date = LocalDate.now().plusDays(1);

        List<ThemeSlot> slots = reservationTimeService.findThemeSlotBy(theme.getId(), date);

        assertThat(slots).hasSize(2);
        assertThat(slots).allMatch(slot -> !slot.isReserved());
    }

    @Test
    @DisplayName("해당 테마, 날짜에 대한 슬롯이 이미 존재하면 DB에서 그대로 조회하여 반환한다.")
    void findThemeSlotBy_returnsExisting_whenExists() {
        Theme theme = fakeThemeDao.save(new Theme("테마1", "설명", "test.com"));
        Time time = fakeTimeDao.save(Time.of(LocalTime.of(10, 0)));
        LocalDate date = LocalDate.now().plusDays(1);

        fakeThemeSlotDao.save(new ThemeSlot(theme, date, time, false));

        List<ThemeSlot> slots = reservationTimeService.findThemeSlotBy(theme.getId(), date);

        assertThat(slots).hasSize(1);
    }

    @Test
    @DisplayName("슬롯을 처음 생성할 때 테마가 존재하지 않으면 예외가 발생한다.")
    void findThemeSlotBy_throwsException_whenThemeNotFound() {
        long nonExistentThemeId = 999L;
        LocalDate date = LocalDate.now().plusDays(1);

        assertThatThrownBy(() -> reservationTimeService.findThemeSlotBy(nonExistentThemeId, date))
                .isInstanceOf(CustomException.class);
    }
}