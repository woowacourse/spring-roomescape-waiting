package roomescape.waiting.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
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
import roomescape.waiting.application.WaitingService;
import roomescape.waiting.application.dto.request.WaitingRequest;
import roomescape.waiting.application.dto.response.WaitingResponse;
import roomescape.waiting.adapter.in.web.WaitingController;

@ExtendWith(MockitoExtension.class)
class WaitingControllerTest {

    @Mock
    private WaitingService waitingService;

    @InjectMocks
    private WaitingController waitingController;

    @Test
    void 대기_생성_응답_테스트() {
        WaitingRequest request = new WaitingRequest(LocalDate.of(2026, 5, 5), 1L, 1L);
        AuthenticatedMember member = AuthenticatedMember.of(2L, Role.USER);
        WaitingResponse serviceResponse = new WaitingResponse(10L, 2L, 1L, 1L);
        when(waitingService.save(request, member.id())).thenReturn(serviceResponse);

        ResponseEntity<ApiResponse<WaitingResponse>> response = waitingController.save(request, member);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isTrue();
        assertThat(response.getBody().data()).isEqualTo(serviceResponse);
    }

    @Test
    void 대기_삭제_응답_테스트() {
        AuthenticatedMember member = AuthenticatedMember.of(2L, Role.USER);

        ResponseEntity<ApiResponse<Void>> response = waitingController.deleteByUser(10L, member);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(response.getBody()).isNull();
        verify(waitingService).deleteByIdForUser(10L, member.id());
    }
}
