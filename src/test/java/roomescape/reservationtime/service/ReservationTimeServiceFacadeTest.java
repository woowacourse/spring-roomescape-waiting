package roomescape.reservationtime.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import roomescape.exception.BadRequestException;
import roomescape.reservation.service.ReservationService;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.domain.ReservationTimeFixture;
import roomescape.reservationtime.dto.request.ReservationTimeCreateRequest;
import roomescape.reservationtime.dto.response.ReservationTimeResponse;
import roomescape.reservationtime.dto.response.ReservationTimeResponseWithBookedStatus;

@SpringBootTest
class ReservationTimeServiceFacadeTest {

    @Autowired
    private ReservationTimeServiceFacade reservationTimeServiceFacade;

    @MockitoBean
    private ReservationTimeService reservationTimeService;

    @MockitoBean
    private ReservationService reservationService;

    @Test
    void 예약_시간을_생성할_수_있다() {
        // given
        LocalTime startAt = LocalTime.of(10, 0);
        ReservationTimeCreateRequest request = new ReservationTimeCreateRequest(startAt);
        ReservationTimeResponse expected = new ReservationTimeResponse(1L, startAt);

        when(reservationTimeService.create(request)).thenReturn(expected);

        // when
        ReservationTimeResponse actual = reservationTimeServiceFacade.createReservationTime(request);

        // then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void 모든_예약_시간을_조회할_수_있다() {
        // given
        List<ReservationTime> allReservationTimes = IntStream.rangeClosed(0, 8)
            .mapToObj(i -> ReservationTimeFixture.create())
            .toList();

        List<ReservationTimeResponse> expected = allReservationTimes.stream()
            .map(ReservationTimeResponse::fromReservationTime)
            .toList();

        when(reservationTimeService.findAll()).thenReturn(expected);

        // when
        List<ReservationTimeResponse> actual = reservationTimeServiceFacade.findAll();

        // then
        assertThat(actual).containsExactlyInAnyOrderElementsOf(expected);
    }

    @Test
    void 예약_시간_id_를_통해_예약_시간을_삭제할_수_있다() {
        // given
        Long reservationTimeId = 1L;

        // when
        reservationTimeServiceFacade.deleteReservationTimeById(reservationTimeId);

        // then
        verify(reservationTimeService).deleteById(reservationTimeId);
    }

    @Test
    void 예약_시간_삭제_시_예약에서_사용_중인_시간이라면_예외가_발생한다() {
        // given
        Long reservationTimeId = 1L;
        // 예외 던지도록
        doThrow(new BadRequestException("테스트 용 예외"))
            .when(reservationService)
            .validateReservationNonExistenceByTimeId(reservationTimeId);

        // when & then
        assertThatThrownBy(() -> reservationTimeServiceFacade.deleteReservationTimeById(reservationTimeId))
            .isInstanceOf(BadRequestException.class)
            .hasMessage("테스트 용 예외");
    }

    @Test
    void 날짜와_테마_id_를_기준으로_모든_예약_시간을_예약_가능_여부와_함꼐_조회할_수_있다() {
        // given
        LocalDate date = LocalDate.now();
        Long themeId = 1L;

        List<ReservationTime> notBookedReservationTimes = IntStream.rangeClosed(0, 4)
            .mapToObj(i -> ReservationTimeFixture.create())
            .toList();
        List<ReservationTime> bookedReservationTimes = IntStream.rangeClosed(0, 3)
            .mapToObj(i -> ReservationTimeFixture.create())
            .toList();

        List<ReservationTime> allTimes = Stream.concat(
            notBookedReservationTimes.stream(),
            bookedReservationTimes.stream()
        ).toList();

        List<ReservationTimeResponseWithBookedStatus> expected = allTimes.stream()
            .map(time ->
                new ReservationTimeResponseWithBookedStatus(
                    time.getId(),
                    time.getStartAt(),
                    !notBookedReservationTimes.contains(time)
                )
            ).toList();

        when(reservationTimeService.findAvailableReservationTimesByDateAndThemeId(date, themeId))
            .thenReturn(expected);

        // when
        List<ReservationTimeResponseWithBookedStatus> actual =
            reservationTimeServiceFacade.findAvailableReservationTimesByDateAndThemeId(date, themeId);

        // then
        assertThat(actual).containsExactlyInAnyOrderElementsOf(expected);
    }
}
