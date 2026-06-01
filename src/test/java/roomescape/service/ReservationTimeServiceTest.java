package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.ServiceTest;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.dto.request.ReservationTimeRequest;
import roomescape.dto.response.AvailableReservationTimeResponse;
import roomescape.dto.response.ReservationTimeResponse;
import roomescape.exception.code.ReservationErrorCode;
import roomescape.exception.code.ReservationTimeErrorCode;
import roomescape.exception.code.ThemeErrorCode;
import roomescape.exception.domain.ReservationException;
import roomescape.exception.domain.ReservationTimeException;
import roomescape.exception.domain.ThemeException;

class ReservationTimeServiceTest extends ServiceTest {

    @Autowired
    private ReservationTimeService reservationTimeService;

    @Test
    void 예약_시간을_생성할_수_있다() {
        // given
        LocalTime startAt = LocalTime.of(10, 0);
        ReservationTimeRequest request = new ReservationTimeRequest(startAt);

        // when
        ReservationTimeResponse response = reservationTimeService.create(request);

        // then
        assertThat(response.startAt()).isEqualTo(startAt);
    }

    @Test
    void 이미_존재하는_예약_시간을_저장_시_예외를_반환한다() {
        // given
        LocalTime startAt = LocalTime.of(10, 0);
        fixtureGenerator.saveReservationTime(startAt);
        ReservationTimeRequest request = new ReservationTimeRequest(startAt);

        // when & then
        assertThatThrownBy(() -> reservationTimeService.create(request))
                .isInstanceOf(ReservationTimeException.class)
                .hasMessage(ReservationTimeErrorCode.RESERVATION_TIME_ALREADY_EXISTS.getMessage());
    }

    @Test
    void 테마_및_날짜에_따른_예약시간을_조회할_수_있다() {
        // given
        Theme theme = fixtureGenerator.saveTheme("테마1", "설명", "https://dsf.sdaf");
        LocalDate baseDate = LocalDate.of(2026, 5, 31);

        ReservationTime reservedTime = fixtureGenerator.saveReservationTime(LocalTime.of(10, 0));
        ReservationTime notReservedTime = fixtureGenerator.saveReservationTime(LocalTime.of(11, 0));

        fixtureGenerator.saveReservation("예약1", baseDate, reservedTime, theme);

        // when
        LocalDate currentDate = LocalDate.of(2026, 5, 30);
        List<AvailableReservationTimeResponse> responses =
                reservationTimeService.getReservationTimes(theme.getId(), baseDate, currentDate);

        // then
        assertThat(responses)
                .extracting(
                        AvailableReservationTimeResponse::id,
                        AvailableReservationTimeResponse::startAt,
                        AvailableReservationTimeResponse::reserved
                )
                .containsExactlyInAnyOrder(
                        tuple(reservedTime.getId(), reservedTime.getStartAt(), true),
                        tuple(notReservedTime.getId(), notReservedTime.getStartAt(), false)
                );
    }

    @Test
    void 예약시간_조회시_날짜가_오늘_이전이면_예외가_발생한다() {
        // given
        Theme theme = fixtureGenerator.saveTheme("테마1", "설명", "https://dsf.sdaf");
        LocalDate today = LocalDate.of(2026, 5, 31);
        LocalDate invalidDate = today.minusDays(1);

        // when & then
        assertThatThrownBy(() -> reservationTimeService.getReservationTimes(theme.getId(), invalidDate, today))
                .isInstanceOf(ReservationException.class)
                .hasMessage(ReservationErrorCode.PAST_DATE_NOT_ALLOWED.getMessage());
    }

    @Test
    void 예약시간_조회시_테마가_존재하지_않으면_예외가_발생한다() {
        // given
        long invalidThemeId = 0L;
        LocalDate today = LocalDate.of(2026, 5, 31);

        // when & then
        assertThatThrownBy(() -> reservationTimeService.getReservationTimes(invalidThemeId, today, today))
                .isInstanceOf(ThemeException.class)
                .hasMessage(ThemeErrorCode.THEME_NOT_FOUND.getMessage());
    }

    @Test
    void 예약시간을_삭제할_수_있다() {
        // given
        Theme theme = fixtureGenerator.saveTheme("테마1", "설명", "https://dsf.sdaf");
        LocalDate today = LocalDate.of(2026, 5, 31);
        LocalTime startAt = LocalTime.of(10, 0);

        ReservationTimeRequest request = new ReservationTimeRequest(startAt);
        ReservationTimeResponse response = reservationTimeService.create(request);

        int beforeSize = reservationTimeService.getReservationTimes(theme.getId(), today, today).size();

        // when
        reservationTimeService.delete(response.id());

        // then
        List<AvailableReservationTimeResponse> reservations = reservationTimeService.getReservationTimes(theme.getId(), today, today);
        assertAll(
                () -> assertThat(reservations).hasSize(beforeSize - 1),
                () -> assertThat(reservations)
                        .extracting(AvailableReservationTimeResponse::id)
                        .doesNotContain(response.id())
        );
    }

    @Test
    void 예약시간_삭제시_관련_예약이_존재하면_예외를_반환한다() {
        // given
        ReservationTime reservationTime = fixtureGenerator.saveReservationTime(LocalTime.of(10, 0));
        LocalDate date = LocalDate.of(2026, 5, 8);
        Theme theme = fixtureGenerator.saveTheme("테마1", "설명", "https://dsf.sdaf");
        fixtureGenerator.saveReservation("예약1", date, reservationTime, theme);

        // when & then
        assertThatThrownBy(() -> reservationTimeService.delete(reservationTime.getId()))
                .isInstanceOf(ReservationTimeException.class)
                .hasMessage(ReservationTimeErrorCode.RESERVATION_TIME_HAS_RESERVATION.getMessage());
    }

    @Test
    void 삭제할_예약시간이_존재하지_않으면_예외를_반환한다() {
        // when & then
        assertThatThrownBy(() -> reservationTimeService.delete(0L))
                .isInstanceOf(ReservationTimeException.class)
                .hasMessage(ReservationTimeErrorCode.RESERVATION_TIME_NOT_FOUND.getMessage());
    }
}
