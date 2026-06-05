package roomescape.slot.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import roomescape.common.api.ApiResponse;
import roomescape.slot.adapter.in.web.ManagerSlotController;
import roomescape.slot.application.dto.request.SlotSaveRequest;
import roomescape.slot.application.dto.response.SlotFindResponse;
import roomescape.slot.application.dto.response.SlotSaveResponse;
import roomescape.slot.application.port.in.CreateSlotUseCase;
import roomescape.slot.application.port.in.DeleteSlotUseCase;
import roomescape.slot.application.port.in.FindSlotUseCase;

@ExtendWith(MockitoExtension.class)
class SlotControllerTest {

    @Mock
    private CreateSlotUseCase createSlotUseCase;
    @Mock
    private FindSlotUseCase findSlotUseCase;
    @Mock
    private DeleteSlotUseCase deleteSlotUseCase;

    @InjectMocks
    private ManagerSlotController managerSlotController;

    @Test
    @DisplayName("슬롯 생성 응답을 반환한다.")
    void returns_slot_create_response() {
        SlotSaveRequest request = new SlotSaveRequest(LocalDate.of(2026, 5, 5), 1L, 1L);
        SlotSaveResponse serviceResponse = new SlotSaveResponse(1L, LocalDate.of(2026, 5, 5), 1L, 1L);
        when(createSlotUseCase.save(request)).thenReturn(serviceResponse);

        ResponseEntity<ApiResponse<SlotSaveResponse>> response = managerSlotController.save(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().data()).isEqualTo(serviceResponse);
    }

    @Test
    @DisplayName("슬롯 목록 조회 응답을 반환한다.")
    void returns_slot_list_response() {
        List<SlotFindResponse> serviceResponse = List.of(
                new SlotFindResponse(1L, LocalDate.of(2026, 5, 5), 1L, 1L)
        );
        when(findSlotUseCase.findAll()).thenReturn(serviceResponse);

        ResponseEntity<ApiResponse<List<SlotFindResponse>>> response = managerSlotController.findAll();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().data()).isEqualTo(serviceResponse);
    }

    @Test
    @DisplayName("슬롯 삭제 응답을 반환한다.")
    void returns_slot_delete_response() {
        ResponseEntity<ApiResponse<Void>> response = managerSlotController.delete(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(response.getBody()).isNull();
        verify(deleteSlotUseCase).deleteById(1L);
    }
}
