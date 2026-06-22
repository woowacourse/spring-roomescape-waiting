package roomescape.reservation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import roomescape.common.api.ApiResponse;
import roomescape.member.domain.AuthenticatedMember;
import roomescape.member.domain.Role;
import roomescape.reservation.adapter.in.web.ManagerReservationController;
import roomescape.reservation.adapter.in.web.UserReservationController;
import roomescape.reservation.application.dto.request.ReservationSaveRequest;
import roomescape.reservation.application.dto.response.ReservationDetailFindResponse;
import roomescape.reservation.application.dto.response.ReservationSaveResponse;
import roomescape.reservation.application.port.in.CancelReservationUseCase;
import roomescape.reservation.application.port.in.CreateReservationUseCase;
import roomescape.reservation.application.port.in.FindReservationUseCase;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservationtime.application.dto.response.TimeInformation;
import roomescape.theme.application.dto.response.ThemeFindResponse;

@ExtendWith(MockitoExtension.class)
class ReservationControllerTest {

    @Mock
    private CreateReservationUseCase createReservationUseCase;

    @Mock
    private CancelReservationUseCase cancelReservationUseCase;

    @Mock
    private FindReservationUseCase findReservationUseCase;

    private UserReservationController userReservationController;
    private ManagerReservationController managerReservationController;

    @BeforeEach
    void setUp() {
        userReservationController = new UserReservationController(createReservationUseCase, cancelReservationUseCase,
                findReservationUseCase);
        managerReservationController = new ManagerReservationController(findReservationUseCase,
                cancelReservationUseCase);
    }

    @Test
    @DisplayName("유저 예약 생성 응답을 반환한다.")
    void returns_user_reservation_create_response() {
        ReservationSaveRequest request = new ReservationSaveRequest(LocalDate.of(2026, 5, 5), 1L, 1L);
        AuthenticatedMember member = AuthenticatedMember.of(1L, Role.USER);
        ReservationSaveResponse serviceResponse = new ReservationSaveResponse(5L, 1L, 4L);
        when(createReservationUseCase.save(request, member.id())).thenReturn(serviceResponse);

        ResponseEntity<ApiResponse<ReservationSaveResponse>> response = userReservationController.save(request, member);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isTrue();
        assertThat(response.getBody().data()).isEqualTo(serviceResponse);
    }

    @Test
    @DisplayName("유저 예약 삭제 응답을 반환한다.")
    void returns_user_reservation_delete_response() {
        AuthenticatedMember member = AuthenticatedMember.of(1L, Role.USER);

        ResponseEntity<ApiResponse<Void>> response = userReservationController.deleteByUser(1L, member);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(response.getBody()).isNull();
        verify(cancelReservationUseCase).deleteByIdForUser(1L, member.id());
    }

    @Test
    @DisplayName("매니저 예약 목록 조회 응답을 반환한다.")
    void returns_manager_reservation_list_response() {
        List<ReservationDetailFindResponse> serviceResponse = List.of(reservationDetailResponse());
        when(findReservationUseCase.findReservationDetails()).thenReturn(serviceResponse);

        ResponseEntity<ApiResponse<List<ReservationDetailFindResponse>>> response =
                managerReservationController.findReservationDetails();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isTrue();
        assertThat(response.getBody().data()).isEqualTo(serviceResponse);
    }

    private ReservationDetailFindResponse reservationDetailResponse() {
        return new ReservationDetailFindResponse(
                1L,
                "a",
                LocalDate.of(2026, 5, 5),
                new ThemeFindResponse(1L, "theme", "description", "thumbnail"),
                new TimeInformation(1L, java.time.LocalTime.of(10, 0)),
                ReservationStatus.CONFIRMED,
                null,
                "order-id",
                10_000,
                "payment-key"
        );
    }
}
