package roomescape.reservationtime.adapter.in.web;

import jakarta.validation.Valid;
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
import roomescape.reservationtime.application.dto.request.ReservationTimeSaveRequest;
import roomescape.reservationtime.application.dto.response.ReservationTimeFindResponse;
import roomescape.reservationtime.application.dto.response.ReservationTimeSaveResponse;
import roomescape.reservationtime.application.port.in.CreateReservationTimeUseCase;
import roomescape.reservationtime.application.port.in.DeleteReservationTimeUseCase;
import roomescape.reservationtime.application.port.in.FindReservationTimeUseCase;

@RestController
@RequestMapping("/api/manager/times")
@RequiredArgsConstructor
public class ManagerReservationTimeController {
    private final CreateReservationTimeUseCase createReservationTimeUseCase;
    private final FindReservationTimeUseCase findReservationTimeUseCase;
    private final DeleteReservationTimeUseCase deleteReservationTimeUseCase;

    @PostMapping
    public ResponseEntity<ApiResponse<ReservationTimeSaveResponse>> save(
            @RequestBody @Valid ReservationTimeSaveRequest body
    ) {
        ReservationTimeSaveResponse response = createReservationTimeUseCase.save(body);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ReservationTimeFindResponse>>> findAll() {
        List<ReservationTimeFindResponse> responses = findReservationTimeUseCase.findAll();
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(responses));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        deleteReservationTimeUseCase.delete(id);
        return ResponseEntity.noContent().build();
    }
}
