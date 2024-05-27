package roomescape.controller.api.admin;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.domain.ReservationTime;
import roomescape.service.dto.request.ReservationTimeSaveRequest;
import roomescape.service.dto.response.ReservationTimeResponse;
import roomescape.service.reservationtime.ReservationTimeService;

import java.net.URI;

@RequestMapping("/api/admin/times")
@RestController
public class AdminReservationTimeApiController {

    private final ReservationTimeService reservationTimeService;

    public AdminReservationTimeApiController(ReservationTimeService reservationTimeService) {
        this.reservationTimeService = reservationTimeService;
    }

    @PostMapping
    public ResponseEntity<ReservationTimeResponse> addReservationTime(@RequestBody @Valid
                                                                      ReservationTimeSaveRequest request) {
        ReservationTime reservationTime = reservationTimeService.createReservationTime(request);
        return ResponseEntity.created(URI.create("api/times/" + reservationTime.getId()))
                .body(new ReservationTimeResponse(reservationTime));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReservationTime(@PathVariable
                                                      @Positive(message = "1 이상의 값만 입력해주세요.")
                                                      long id) {
        reservationTimeService.deleteReservationTime(id);
        return ResponseEntity.noContent().build();
    }
}
