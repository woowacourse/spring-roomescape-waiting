package roomescape.slot.service;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import roomescape.date.domain.ReservationDate;
import roomescape.date.fixture.ReservationDateFixture;
import roomescape.slot.domain.ReservationSlot;
import roomescape.slot.service.dto.SlotSaveCommand;
import roomescape.support.ServiceSupport;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.ReservationTime;
import roomescape.time.fixture.ReservationTimeFixture;

@Import(ReservationSlotService.class)
class ReservationSlotServiceTest extends ServiceSupport {

    @Autowired
    private ReservationSlotService reservationSlotService;

    @Test
    @DisplayName("테마/날짜/시간으로 슬롯을 생성한다.")
    void save() {
        // given
        ReservationDate date = saveDate(ReservationDateFixture.activeOneWeekLater());
        ReservationTime time = saveTime(ReservationTimeFixture.activeTime15());
        Theme theme = saveTheme("테마1");

        SlotSaveCommand slotSaveCommand = new SlotSaveCommand(date.getId(), time.getId(), theme.getId());
        ReservationSlot expected = ReservationSlot.of(date, time, theme);

        // when
        ReservationSlot actual = reservationSlotService.save(slotSaveCommand);

        // then
        Assertions.assertThat(actual)
                .usingRecursiveComparison()
                .ignoringFields("id")
                .isEqualTo(expected);
    }

}
