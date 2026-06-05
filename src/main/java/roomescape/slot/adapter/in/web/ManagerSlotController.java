package roomescape.slot.adapter.in.web;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.common.api.ApiResponse;
import roomescape.slot.application.dto.request.SlotSaveRequest;
import roomescape.slot.application.dto.response.SlotFindResponse;
import roomescape.slot.application.dto.response.SlotSaveResponse;
import roomescape.slot.application.port.in.CreateSlotUseCase;
import roomescape.slot.application.port.in.DeleteSlotUseCase;
import roomescape.slot.application.port.in.FindSlotUseCase;

@RestController
@RequestMapping("/api/manager/slots")
@RequiredArgsConstructor
public class ManagerSlotController {

    private final CreateSlotUseCase createSlotUseCase;
    private final FindSlotUseCase findSlotUseCase;
    private final DeleteSlotUseCase deleteSlotUseCase;

    @PostMapping
    public ResponseEntity<ApiResponse<SlotSaveResponse>> save(
            @RequestBody @Valid SlotSaveRequest body
    ) {
        SlotSaveResponse response = createSlotUseCase.save(body);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<SlotFindResponse>>> findAll() {
        List<SlotFindResponse> response = findSlotUseCase.findAll();
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(response));
    }

    @GetMapping("/{slotId}")
    public ResponseEntity<ApiResponse<SlotFindResponse>> findById(
            @PathVariable @Positive long slotId
    ) {
        SlotFindResponse response = findSlotUseCase.findById(slotId);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(response));
    }

    @DeleteMapping("/{slotId}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable @Positive long slotId
    ) {
        deleteSlotUseCase.deleteById(slotId);
        return ResponseEntity.noContent().build();
    }
}
