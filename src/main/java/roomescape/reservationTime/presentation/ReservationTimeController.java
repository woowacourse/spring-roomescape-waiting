package roomescape.reservationTime.presentation;

import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.time.format.DateTimeParseException;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.common.exception.handler.dto.ExceptionResponse;
import roomescape.reservationTime.presentation.dto.ReservationTimeRequest;
import roomescape.reservationTime.presentation.dto.ReservationTimeResponse;
import roomescape.reservationTime.presentation.dto.TimeConditionRequest;
import roomescape.reservationTime.presentation.dto.TimeConditionResponse;
import roomescape.reservationTime.service.ReservationTimeService;

@RestController
@RequestMapping("/times")
public class ReservationTimeController {

    public static final String GET_ADMIN_TIME = "/admin/time";

    private final ReservationTimeService reservationTimeService;

    public ReservationTimeController(final ReservationTimeService reservationTimeService) {
        this.reservationTimeService = reservationTimeService;
    }

    @GetMapping
    public ResponseEntity<List<ReservationTimeResponse>> getReservationTimes() {
        List<ReservationTimeResponse> response = reservationTimeService.getReservationTimes();
        return ResponseEntity.ok(response);
    }

    @GetMapping(consumes = {"application/json"})
    public ResponseEntity<List<TimeConditionResponse>> getReservationTimes(final TimeConditionRequest request) {
        List<TimeConditionResponse> responses = reservationTimeService.getTimesWithCondition(request);
        return ResponseEntity.ok().body(responses);
    }

    @PostMapping
    public ResponseEntity<ReservationTimeResponse> createReservationTime(@RequestBody final ReservationTimeRequest request) {
        ReservationTimeResponse response = reservationTimeService.createReservationTime(request);
        return ResponseEntity.created(URI.create(GET_ADMIN_TIME)).body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReservationTimeById(@PathVariable("id") final Long id) {
        reservationTimeService.deleteReservationTimeById(id);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(value = DateTimeParseException.class)
    public ResponseEntity<ExceptionResponse> noMatchTimeType(final HttpServletRequest request) {
        ExceptionResponse exceptionResponse = new ExceptionResponse(
                400, "[ERROR] 요청 시간 형식이 맞지 않습니다.", request.getRequestURI()
        );
        return ResponseEntity.badRequest().body(exceptionResponse);
    }
}
