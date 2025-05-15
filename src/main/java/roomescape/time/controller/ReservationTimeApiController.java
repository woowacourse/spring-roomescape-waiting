package roomescape.time.controller;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static roomescape.time.controller.response.TimeSuccessCode.CREATE_TIME;
import static roomescape.time.controller.response.TimeSuccessCode.DELETE_TIME;
import static roomescape.time.controller.response.TimeSuccessCode.GET_AVAILABLE_TIMES;
import static roomescape.time.controller.response.TimeSuccessCode.GET_TIMES;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.global.response.ApiResponse;
import roomescape.time.controller.request.AvailableReservationTimeRequest;
import roomescape.time.controller.request.ReservationTimeCreateRequest;
import roomescape.time.controller.response.AvailableReservationTimeResponse;
import roomescape.time.controller.response.ReservationTimeResponse;
import roomescape.time.service.ReservationTimeService;

@RestController
@RequestMapping("/times")
public class ReservationTimeApiController {

    private final ReservationTimeService reservationTimeService;

    public ReservationTimeApiController(ReservationTimeService reservationTimeService) {
        this.reservationTimeService = reservationTimeService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ReservationTimeResponse>> createTime(
            @RequestBody @Valid ReservationTimeCreateRequest request
    ) {
        ReservationTimeResponse response = reservationTimeService.open(request);

        return ResponseEntity
                .status(CREATED)
                .body(ApiResponse.success(CREATE_TIME, response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ReservationTimeResponse>>> getTimes() {
        List<ReservationTimeResponse> responses = reservationTimeService.getAll();

        return ResponseEntity.ok(
                ApiResponse.success(GET_TIMES, responses));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTime(@PathVariable Long id) {
        reservationTimeService.deleteById(id);

        return ResponseEntity
                .status(NO_CONTENT)
                .body(ApiResponse.success(DELETE_TIME));
    }

    @GetMapping("/available")
    public ResponseEntity<ApiResponse<List<AvailableReservationTimeResponse>>> getAvailableReservationTimes(
            @ModelAttribute @Valid AvailableReservationTimeRequest request
    ) {
        List<AvailableReservationTimeResponse> responses =
                reservationTimeService.getAvailableReservationTimes(request);

        return ResponseEntity
                .ok(ApiResponse.success(GET_AVAILABLE_TIMES, responses));
    }
}
