package roomescape.controller.api.admin;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.domain.ReservationTime;
import roomescape.service.dto.request.ReservationTimeSaveRequest;
import roomescape.service.dto.response.ReservationTimeResponse;
import roomescape.service.reservationtime.ReservationTimeCreateService;
import roomescape.service.reservationtime.ReservationTimeDeleteService;

import java.net.URI;

@RestController
public class AdminReservationTimeApiController {

    private final ReservationTimeCreateService reservationTimeCreateService;
    private final ReservationTimeDeleteService reservationTimeDeleteService;

    public AdminReservationTimeApiController(ReservationTimeCreateService reservationTimeCreateService,
                                             ReservationTimeDeleteService reservationTimeDeleteService) {
        this.reservationTimeCreateService = reservationTimeCreateService;
        this.reservationTimeDeleteService = reservationTimeDeleteService;
    }

    @PostMapping("/api/admin/times")
    public ResponseEntity<ReservationTimeResponse> addReservationTime(@RequestBody @Valid
                                                                      ReservationTimeSaveRequest request) {
        ReservationTime reservationTime = reservationTimeCreateService.createReservationTime(request);
        return ResponseEntity.created(URI.create("api/times/" + reservationTime.getId()))
                .body(new ReservationTimeResponse(reservationTime));
    }

    @DeleteMapping("/api/admin/times/{id}")
    public ResponseEntity<Void> deleteReservationTime(@PathVariable
                                                      @Positive(message = "1 이상의 값만 입력해주세요.")
                                                      long id) {
        reservationTimeDeleteService.deleteReservationTime(id);
        return ResponseEntity.noContent().build();
    }
}
