package roomescape.waiting;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import roomescape.common.api.ApiResponse;
import roomescape.member.domain.AuthenticatedMember;
import roomescape.member.domain.Role;
import roomescape.waiting.adapter.in.web.WaitingController;
import roomescape.waiting.application.dto.request.WaitingRequest;
import roomescape.waiting.application.dto.response.WaitingResponse;
import roomescape.waiting.application.port.in.CancelWaitingUseCase;
import roomescape.waiting.application.port.in.CreateWaitingUseCase;

@ExtendWith(MockitoExtension.class)
class WaitingControllerTest {

    @Mock
    private CreateWaitingUseCase createWaitingUseCase;

    @Mock
    private CancelWaitingUseCase cancelWaitingUseCase;

    @InjectMocks
    private WaitingController waitingController;

    @Test
    @DisplayName("대기 생성 응답을 반환한다.")
    void returns_waiting_create_response() {
        WaitingRequest request = new WaitingRequest(LocalDate.of(2026, 5, 5), 1L, 1L);
        AuthenticatedMember member = AuthenticatedMember.of(2L, Role.USER);
        WaitingResponse serviceResponse = new WaitingResponse(10L, 2L, 1L, 1L);
        when(createWaitingUseCase.save(request, member.id())).thenReturn(serviceResponse);

        ResponseEntity<ApiResponse<WaitingResponse>> response = waitingController.save(request, member);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isTrue();
        assertThat(response.getBody().data()).isEqualTo(serviceResponse);
    }

    @Test
    @DisplayName("대기 삭제 응답을 반환한다.")
    void returns_waiting_delete_response() {
        AuthenticatedMember member = AuthenticatedMember.of(2L, Role.USER);

        ResponseEntity<ApiResponse<Void>> response = waitingController.deleteByUser(10L, member);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(response.getBody()).isNull();
        verify(cancelWaitingUseCase).deleteByIdForUser(10L, member.id());
    }
}
