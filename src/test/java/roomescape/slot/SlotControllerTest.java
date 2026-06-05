package roomescape.slot.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import roomescape.common.api.ApiResponse;
import roomescape.slot.application.SlotService;
import roomescape.slot.application.dto.request.SlotSaveRequest;
import roomescape.slot.application.dto.response.SlotFindResponse;
import roomescape.slot.application.dto.response.SlotSaveResponse;
import roomescape.slot.adapter.in.web.ManagerSlotController;

@ExtendWith(MockitoExtension.class)
class SlotControllerTest {

    @Mock
    private SlotService slotService;

    @InjectMocks
    private ManagerSlotController managerSlotController;

    @Test
    void 슬롯_생성_응답_테스트() {
        SlotSaveRequest request = new SlotSaveRequest(LocalDate.of(2026, 5, 5), 1L, 1L);
        SlotSaveResponse serviceResponse = new SlotSaveResponse(1L, LocalDate.of(2026, 5, 5), 1L, 1L);
        when(slotService.save(request)).thenReturn(serviceResponse);

        ResponseEntity<ApiResponse<SlotSaveResponse>> response = managerSlotController.save(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().data()).isEqualTo(serviceResponse);
    }

    @Test
    void 슬롯_목록_조회_응답_테스트() {
        List<SlotFindResponse> serviceResponse = List.of(
                new SlotFindResponse(1L, LocalDate.of(2026, 5, 5), 1L, 1L)
        );
        when(slotService.findAll()).thenReturn(serviceResponse);

        ResponseEntity<ApiResponse<List<SlotFindResponse>>> response = managerSlotController.findAll();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().data()).isEqualTo(serviceResponse);
    }

    @Test
    void 슬롯_삭제_응답_테스트() {
        ResponseEntity<ApiResponse<Void>> response = managerSlotController.delete(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(response.getBody()).isNull();
        verify(slotService).deleteById(1L);
    }
}
