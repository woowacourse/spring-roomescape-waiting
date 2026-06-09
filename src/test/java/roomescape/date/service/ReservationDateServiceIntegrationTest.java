package roomescape.date.service;


import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import roomescape.date.domain.ReservationDate;
import roomescape.date.exception.ReservationDateException;
import roomescape.date.fixture.ReservationDateFixture;
import roomescape.slot.domain.ReservationSlot;
import roomescape.support.ServiceSupport;
import roomescape.theme.domain.Theme;
import roomescape.theme.fixture.ThemeFixture;
import roomescape.time.domain.ReservationTime;
import roomescape.time.fixture.ReservationTimeFixture;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static roomescape.date.exception.ReservationDateErrorInformation.DATE_ALREADY_EXISTS;
import static roomescape.date.exception.ReservationDateErrorInformation.DATE_NOT_FOUND;

@Import(ReservationDateService.class)
class ReservationDateServiceIntegrationTest extends ServiceSupport {

    private static final LocalDate DEFAULT_DATE = LocalDate.of(2099, 1, 1);

    @Autowired
    private ReservationDateService reservationDateService;

    @Test
    @DisplayName("등록된 예약날짜가 여러개이면 조회 시 등록된 개수만큼 반환한다.")
    void readDates() {
        // given
        List<ReservationDate> reservationDates = List.of(
                ReservationDateFixture.oneWeekLater(),
                ReservationDateFixture.twoWeeksLater()
        );
        saveAll(reservationDates);

        // when
        List<ReservationDate> actual = reservationDateService.readDates();

        // then
        Assertions.assertThat(actual)
                .hasSize(reservationDates.size());
    }

    @Test
    @DisplayName("등록된 예약날짜와 조회된 예약날짜의 모든 필드는 일치한다")
    void readDate() {
        // given
        ReservationDate saved = saveDate(ReservationDateFixture.oneWeekLater());

        // when
        ReservationDate actual = reservationDateService.readDate(saved.getId());

        // then
        Assertions.assertThat(actual)
                .usingRecursiveComparison()
                .isEqualTo(saved);
    }

    @Test
    @DisplayName("등록되지 않은 예약날짜를 조회하면 예외가 발생한다.")
    void readDate_deregistered() {
        // given
        Long deregisteredId = Long.MIN_VALUE;

        // when & then
        Assertions.assertThatThrownBy(() -> reservationDateService.readDate(deregisteredId))
                .isInstanceOf(ReservationDateException.class)
                .hasMessage(DATE_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("예약날짜를 1개 등록하면 예약날짜 데이터 수가 1 증가한다.")
    void register() {
        // given
        List<ReservationDate> emptyDates = List.of();

        // when
        reservationDateService.register(DEFAULT_DATE);

        // then
        Assertions.assertThat(reservationDateRepository.findAll())
                .hasSize(emptyDates.size() + 1);
    }

    @Test
    @DisplayName("등록한 예약날짜와 다시 조회한 예약날짜의 모든 필드가 일치한다.")
    void register_theme_fields_match() {
        // when
        ReservationDate registered = reservationDateService.register(DEFAULT_DATE);

        // then
        assertThat(registered)
                .usingRecursiveComparison()
                .isEqualTo(reservationDateRepository.findById(registered.getId()).get());
    }

    @Test
    @DisplayName("등록되지않은 예약날짜의 상태를 변경하면 예외가 발생한다.")
    void updateStatus_deregistered() {
        // given
        Long deregisteredId = Long.MIN_VALUE;

        // when  & then
        Assertions.assertThatThrownBy(() -> reservationDateService.updateStatus(deregisteredId, false))
                .isInstanceOf(ReservationDateException.class)
                .hasMessage(DATE_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("이미 등록된 날짜를 또 등록하면 예외가 발생한다.")
    void existsByDate_duplicated_date() {
        // given
        ReservationDate date = saveDate(ReservationDateFixture.oneWeekLater());
        LocalDate duplicatedDate = date.getDate();

        // when & then
        Assertions.assertThatThrownBy(() -> reservationDateService.register(duplicatedDate))
                .isInstanceOf(ReservationDateException.class)
                .hasMessage(DATE_ALREADY_EXISTS.getMessage());
    }


    @Test
    @DisplayName("특정 테마의 슬롯이 등록된 활성화 날짜를 조회한다.")
    void readSlotOfDatesByThemeId() {
        // given
        Theme theme = saveTheme(ThemeFixture.activeTheme());
        ReservationDate date = saveDate(ReservationDateFixture.activeOneWeekLater());
        ReservationTime time = saveTime(ReservationTimeFixture.activeTime15());
        saveSlot(ReservationSlot.of(date, time, theme));

        // when
        List<ReservationDate> actual = reservationDateService.readSlotOfDatesByThemeId(theme.getId());

        // then
        assertThat(actual)
                .hasSize(1);

        assertThat(actual.get(0))
                .usingRecursiveComparison()
                .isEqualTo(date);
    }

    @Test
    @DisplayName("슬롯이 없는 날짜는 특정 테마의 슬롯 날짜 조회 시 반환되지 않는다.")
    void readSlotOfDatesByThemeId_without_slot() {
        // given
        Theme theme = saveTheme(ThemeFixture.activeTheme());
        saveDate(ReservationDateFixture.activeOneWeekLater());

        // when
        List<ReservationDate> actual = reservationDateService.readSlotOfDatesByThemeId(theme.getId());

        // then
        assertThat(actual)
                .isEmpty();
    }

    @Test
    @DisplayName("다른 테마의 슬롯만 있는 날짜는 조회되지 않는다.")
    void readSlotOfDatesByThemeId_other_theme_slot_not_returned() {
        // given
        Theme theme = saveTheme(ThemeFixture.activeTheme("테마1"));
        Theme otherTheme = saveTheme(ThemeFixture.activeTheme("테마2"));
        ReservationDate date = saveDate(ReservationDateFixture.activeOneWeekLater());
        ReservationTime time = saveTime(ReservationTimeFixture.activeTime15());
        saveSlot(ReservationSlot.of(date, time, otherTheme));

        // when
        List<ReservationDate> actual = reservationDateService.readSlotOfDatesByThemeId(theme.getId());

        // then
        assertThat(actual)
                .isEmpty();
    }

    @Test
    @DisplayName("비활성화 날짜는 슬롯이 있어도 조회되지 않는다.")
    void readSlotOfDatesByThemeId_inactive_date_not_returned() {
        // given
        Theme theme = saveTheme(ThemeFixture.activeTheme());
        ReservationDate inactiveDate = saveDate(ReservationDateFixture.inActiveTwoWeekLater());
        ReservationTime time = saveTime(ReservationTimeFixture.activeTime15());
        saveSlot(ReservationSlot.of(inactiveDate, time, theme));

        // when
        List<ReservationDate> actual = reservationDateService.readSlotOfDatesByThemeId(theme.getId());

        // then
        assertThat(actual)
                .isEmpty();
    }

    @Test
    @DisplayName("동일 날짜에 여러 슬롯이 있어도 한 번만 반환된다.")
    void readSlotOfDatesByThemeId_multiple_slots_distinct() {
        // given
        Theme theme = saveTheme(ThemeFixture.activeTheme());
        ReservationDate date = saveDate(ReservationDateFixture.activeOneWeekLater());
        ReservationTime time1 = saveTime(ReservationTimeFixture.activeTime15());
        ReservationTime time2 = saveTime(ReservationTimeFixture.activeTime16());
        saveSlot(ReservationSlot.of(date, time1, theme));
        saveSlot(ReservationSlot.of(date, time2, theme));

        // when
        List<ReservationDate> actual = reservationDateService.readSlotOfDatesByThemeId(theme.getId());

        // then
        assertThat(actual)
                .hasSize(1);
    }

    private List<ReservationDate> saveAll(List<ReservationDate> dates) {
        List<ReservationDate> saved = new ArrayList<>();
        for (ReservationDate reservationDate : dates) {
            saved.add(saveDate(reservationDate));
        }
        return saved;
    }

}
