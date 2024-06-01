package roomescape.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import roomescape.global.exception.model.AssociatedDataExistsException;
import roomescape.global.exception.model.DataDuplicateException;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.dto.request.ReservationTimeRequest;
import roomescape.reservation.service.ReservationTimeService;
import roomescape.theme.domain.Theme;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThatThrownBy;


@Import(ReservationTimeService.class)
class ReservationTimeServiceTest extends ServiceTest {

    @Autowired
    private ReservationTimeService reservationTimeService;

    @Test
    @DisplayName("중복된 예약 시간을 등록하는 경우 예외가 발생한다.")
    void duplicateTimeFail() {
        // given
        LocalTime time = LocalTime.of(12, 30);
        reservationTimeFixture.createTime(time);

        // when & then
        assertThatThrownBy(() -> reservationTimeService.addTime(new ReservationTimeRequest(time)))
                .isInstanceOf(DataDuplicateException.class);
    }

    @Test
    @DisplayName("삭제하려는 시간에 예약이 존재하면 예외를 발생한다.")
    void usingTimeDeleteFail() {
        // given
        ReservationTime time = reservationTimeFixture.createTime(LocalTime.of(12, 30));
        Theme theme = themeFixture.createTheme();

        // when
        reservationDetailFixture.createReservationDetail(LocalDate.now().plusDays(1L), time, theme);

        // then
        assertThatThrownBy(() -> reservationTimeService.removeTimeById(time.getId()))
                .isInstanceOf(AssociatedDataExistsException.class);
    }
}
