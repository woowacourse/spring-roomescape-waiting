package roomescape.slot.presentation;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
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
import roomescape.slot.application.SlotService;
import roomescape.slot.dto.request.SlotSaveRequest;
import roomescape.slot.dto.response.SlotFindResponse;
import roomescape.slot.dto.response.SlotSaveResponse;

import java.util.List;

@RestController
@RequestMapping("/api/manager/slots")
@RequiredArgsConstructor
public class ManagerSlotController {

    private final SlotService slotService;

    @PostMapping
    public ResponseEntity<ApiResponse<SlotSaveResponse>> save(
            @RequestBody @Valid SlotSaveRequest body
    ) {
        SlotSaveResponse response = slotService.save(body);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<SlotFindResponse>>> findAll() {
        List<SlotFindResponse> response = slotService.findAll();
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(response));
    }

    @GetMapping("/{slotId}")
    public ResponseEntity<ApiResponse<SlotFindResponse>> findById(
            @PathVariable @Positive long slotId
    ) {
        SlotFindResponse response = slotService.findById(slotId);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(response));
    }

    @DeleteMapping("/{slotId}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable @Positive long slotId
    ) {
        slotService.deleteById(slotId);
        return ResponseEntity.noContent().build();
    }
}
