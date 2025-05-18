package roomescape.time.application;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.time.application.dto.CreateReservationTimeServiceRequest;
import roomescape.time.application.service.ReservationTimeCommandService;
import roomescape.time.application.service.ReservationTimeQueryService;
import roomescape.time.domain.ReservationTime;
import roomescape.time.domain.ReservationTimeId;
import roomescape.time.ui.dto.CreateReservationTimeWebRequest;
import roomescape.time.ui.dto.ReservationTimeResponse;

import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class ReservationTimeFacadeImplTest {

    @Mock
    private ReservationTimeQueryService reservationTimeQueryService;

    @Mock
    private ReservationTimeCommandService reservationTimeCommandService;

    @InjectMocks
    private ReservationTimeFacadeImpl reservationTimeFacade;

    @Test
    @DisplayName("모든 예약 시간을 조회한다")
    void getAll() {
        List<ReservationTime> reservationTimes = List.of(
                createReservationTime(1L, LocalTime.of(10, 0)),
                createReservationTime(2L, LocalTime.of(14, 0))
        );
        given(reservationTimeQueryService.getAll()).willReturn(reservationTimes);

        List<ReservationTimeResponse> result = reservationTimeFacade.getAll();

        assertThat(result).hasSize(2);
        then(reservationTimeQueryService).should(times(1)).getAll();
    }

    @Test
    @DisplayName("예약 시간을 생성한다")
    void create() {
        LocalTime startAt = LocalTime.of(16, 0);
        CreateReservationTimeWebRequest request = new CreateReservationTimeWebRequest(startAt);
        ReservationTime reservationTime = createReservationTime(1L, startAt);
        given(reservationTimeCommandService.create(any(CreateReservationTimeServiceRequest.class)))
                .willReturn(reservationTime);

        ReservationTimeResponse result = reservationTimeFacade.create(request);

        assertThat(result).isNotNull();
        then(reservationTimeCommandService).should(times(1))
                .create(any(CreateReservationTimeServiceRequest.class));
    }

    @Test
    @DisplayName("예약 시간을 삭제한다")
    void delete() {
        Long reservationTimeId = 1L;

        reservationTimeFacade.delete(reservationTimeId);

        then(reservationTimeCommandService).should(times(1)).delete(any(ReservationTimeId.class));
    }

    private ReservationTime createReservationTime(Long id, LocalTime startAt) {
        return ReservationTime.withId(
                ReservationTimeId.from(id),
                startAt
        );
    }
}
