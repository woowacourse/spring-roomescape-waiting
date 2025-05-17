package roomescape.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import roomescape.controller.request.CreateReservationAdminRequest;
import roomescape.controller.response.ReservationResponse;
import roomescape.service.ReservationCreationService;
import roomescape.service.param.CreateReservationParam;
import roomescape.service.result.ReservationResult;

@RestController
@RequestMapping("/admin")
public class AdminController {

    public AdminController(ReservationCreationService reservationCreationService) {
        this.reservationCreationService = reservationCreationService;
    }

    private final ReservationCreationService reservationCreationService;


    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/reservations")
    public ReservationResponse createReservation(@RequestBody CreateReservationAdminRequest reservationRequest) {
        CreateReservationParam createReservationParam = new CreateReservationParam(
                reservationRequest.memberId(),
                reservationRequest.date(),
                reservationRequest.timeId(),
                reservationRequest.themeId()
        );
        ReservationResult reservationResult = reservationCreationService.create(createReservationParam);
        return ReservationResponse.from(reservationResult);
    }
}
