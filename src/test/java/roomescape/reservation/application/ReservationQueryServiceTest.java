package roomescape.reservation.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import roomescape.common.exception.impl.NotFoundException;
import roomescape.reservation.application.dto.AvailableReservationTimeResponse;
import roomescape.reservation.application.dto.MyHistoryResponse;

@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@SpringBootTest
class ReservationQueryServiceTest {

    @Autowired
    private ReservationQueryService reservationQueryService;

    @Test
    void 모든_예약기록을_조회한다() {
        assertThat(reservationQueryService.findReservedReservations()).hasSize(8);
    }

    @Test
    void 예약가능한_시간을_조회한다() {
        final Long themeId = 2L;
        final LocalDate date = LocalDate.now().minusDays(3);

        assertThat(reservationQueryService.findAvailableReservationTime(themeId, date))
                .isEqualTo(List.of(
                        new AvailableReservationTimeResponse(1L, LocalTime.of(10, 0), true),
                        new AvailableReservationTimeResponse(2L, LocalTime.of(12, 0), true),
                        new AvailableReservationTimeResponse(3L, LocalTime.of(14, 0), false),
                        new AvailableReservationTimeResponse(4L, LocalTime.of(16, 0), false),
                        new AvailableReservationTimeResponse(5L, LocalTime.of(18, 0), false),
                        new AvailableReservationTimeResponse(6L, LocalTime.of(20, 0), false)
                ));
    }

    @Test
    void 해당기간에서_테마id와_멤버id로_예약을_조회한다() {
        final Long themeId = 2L;
        final Long memberId = 1L;
        final LocalDate start = LocalDate.now().minusDays(10);
        final LocalDate end = LocalDate.now().minusDays(1);

        assertThat(reservationQueryService.findReservationByThemeIdAndMemberIdInDuration(
                themeId, memberId, start, end)).hasSize(2);
    }

    @Test
    void 멤버id로_예약기록을_조회한다() {
        final long memberId = 1L;
        final List<MyHistoryResponse> responses = reservationQueryService.findMyReservation(memberId);

        assertThat(responses).hasSize(4);
    }

    @Test
    void 대기중인_예약을_조회한다() {
        assertThat(reservationQueryService.findWaitingReservations()).hasSize(3);
    }

    @Test
    void 존재하지_않는_테마의_예약가능_시간을_조회할_수_없다() {
        final Long invalidThemeId = 999L;
        final LocalDate date = LocalDate.now();

        assertThatThrownBy(() -> reservationQueryService.findAvailableReservationTime(invalidThemeId, date))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("선택한 테마가 존재하지 않습니다.");
    }

    @Test
    void 기간내_테마와_멤버의_예약이_없으면_빈_목록을_반환한다() {
        final Long themeId = 1L;
        final Long memberId = 999L;
        final LocalDate start = LocalDate.now();
        final LocalDate end = LocalDate.now().plusDays(1);

        assertThat(reservationQueryService.findReservationByThemeIdAndMemberIdInDuration(
                themeId, memberId, start, end)).isEmpty();
    }

    @Test
    void 존재하지_않는_멤버의_예약기록은_빈_목록을_반환한다() {
        final long nonExistingMemberId = 999L;

        assertThat(reservationQueryService.findMyReservation(nonExistingMemberId)).isEmpty();
    }
}
