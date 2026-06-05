package roomescape.reservationtime.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import roomescape.common.api.ApiResponse;
import roomescape.reservationtime.application.port.in.CreateReservationTimeUseCase;
import roomescape.reservationtime.application.port.in.DeleteReservationTimeUseCase;
import roomescape.reservationtime.application.port.in.FindReservationTimeUseCase;
import roomescape.reservationtime.application.dto.request.ReservationTimeSaveRequest;
import roomescape.reservationtime.application.dto.response.AvailableTimeFindResponse;
import roomescape.reservationtime.application.dto.response.ReservationTimeFindResponse;
import roomescape.reservationtime.application.dto.response.ReservationTimeSaveResponse;
import roomescape.reservationtime.application.dto.response.TimeInformation;
import roomescape.reservationtime.application.dto.response.TimeSlotStatus;
import roomescape.reservationtime.adapter.in.web.ManagerReservationTimeController;
import roomescape.reservationtime.adapter.in.web.UserReservationTimeController;

@ExtendWith(MockitoExtension.class)
class ReservationTimeControllerTest {

    @Mock
    private CreateReservationTimeUseCase createReservationTimeUseCase;
    @Mock
    private FindReservationTimeUseCase findReservationTimeUseCase;
    @Mock
    private DeleteReservationTimeUseCase deleteReservationTimeUseCase;

    private UserReservationTimeController userReservationTimeController;
    private ManagerReservationTimeController managerReservationTimeController;

    @BeforeEach
    void setUp() {
        userReservationTimeController = new UserReservationTimeController(findReservationTimeUseCase);
        managerReservationTimeController = new ManagerReservationTimeController(createReservationTimeUseCase, findReservationTimeUseCase, deleteReservationTimeUseCase);
    }

    @Test
    void 예약_가능_시간_조회_응답_테스트() {
        LocalDate date = LocalDate.of(2026, 5, 5);
        List<AvailableTimeFindResponse> serviceResponse = List.of(
                new AvailableTimeFindResponse(new TimeInformation(1L, LocalTime.of(10, 0)), TimeSlotStatus.RESERVABLE)
        );
        when(findReservationTimeUseCase.findTimesByDateAndThemeId(date, 1L)).thenReturn(serviceResponse);

        ResponseEntity<ApiResponse<List<AvailableTimeFindResponse>>> response =
                userReservationTimeController.findTimesByDateAndThemeId(date, 1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().data()).isEqualTo(serviceResponse);
    }

    @Test
    void 시간_생성_응답_테스트() {
        ReservationTimeSaveRequest request = new ReservationTimeSaveRequest(LocalTime.of(10, 0));
        ReservationTimeSaveResponse serviceResponse = new ReservationTimeSaveResponse(1L, LocalTime.of(10, 0));
        when(createReservationTimeUseCase.save(request)).thenReturn(serviceResponse);

        ResponseEntity<ApiResponse<ReservationTimeSaveResponse>> response = managerReservationTimeController.save(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().data()).isEqualTo(serviceResponse);
    }

    @Test
    void 시간_삭제_응답_테스트() {
        ResponseEntity<ApiResponse<Void>> response = managerReservationTimeController.delete(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(response.getBody()).isNull();
        verify(deleteReservationTimeUseCase).delete(1L);
    }

    @Test
    void 시간_목록_조회_응답_테스트() {
        List<ReservationTimeFindResponse> serviceResponse = List.of(
                new ReservationTimeFindResponse(1L, LocalTime.of(10, 0))
        );
        when(findReservationTimeUseCase.findAll()).thenReturn(serviceResponse);

        ResponseEntity<ApiResponse<List<ReservationTimeFindResponse>>> response = managerReservationTimeController.findAll();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().data()).isEqualTo(serviceResponse);
    }
}
