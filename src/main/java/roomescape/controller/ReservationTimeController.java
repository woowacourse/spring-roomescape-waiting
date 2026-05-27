package roomescape.controller;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.controller.dto.request.ControllerReservationTimeCreateRequest;
import roomescape.controller.dto.response.ControllerReservationTimeAvailabilityResponse;
import roomescape.controller.dto.response.ControllerReservationTimeResponse;
import roomescape.facade.ReservationTimeFacade;
import roomescape.service.dto.response.ServiceReservationTimeAvailabilityResponse;
import roomescape.service.dto.response.ServiceReservationTimeResponse;

@RestController
@RequestMapping(value = "/times")
public class ReservationTimeController {

    private final ReservationTimeFacade reservationTimeFacade;

    public ReservationTimeController(ReservationTimeFacade reservationTimeFacade) {
        this.reservationTimeFacade = reservationTimeFacade;
    }


    @PostMapping
    public ResponseEntity<ControllerReservationTimeResponse> save(
            @Valid @RequestBody ControllerReservationTimeCreateRequest requestDto) {
        ServiceReservationTimeResponse serviceResponse = reservationTimeFacade.save(
                requestDto.toServiceReservationTimeRequest());
        ControllerReservationTimeResponse controllerResponse = ControllerReservationTimeResponse.from(serviceResponse);
        return ResponseEntity.
                status(HttpStatus.CREATED)
                .body(controllerResponse);
    }

    @GetMapping
    public ResponseEntity<List<ControllerReservationTimeResponse>> findAll() {
        List<ServiceReservationTimeResponse> serviceResponses = reservationTimeFacade.findAll();
        List<ControllerReservationTimeResponse> controllerResponses = serviceResponses.stream()
                .map(ControllerReservationTimeResponse::from)
                .toList();
        return ResponseEntity.ok(controllerResponses);
    }

    @GetMapping("/available")
    public ResponseEntity<List<ControllerReservationTimeAvailabilityResponse>> findAvailabilityByDateAndTheme(
            @RequestParam("date") LocalDate date, @RequestParam("themeId") Long themeId) {

        List<ServiceReservationTimeAvailabilityResponse> serviceResponses = reservationTimeFacade.findAvailabilityByDateAndTheme(
                date, themeId);

        List<ControllerReservationTimeAvailabilityResponse> controllerResponses = serviceResponses.stream()
                .map(ControllerReservationTimeAvailabilityResponse::from)
                .toList();
        return ResponseEntity.ok(controllerResponses);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {

        reservationTimeFacade.delete(id);
        return ResponseEntity
                .noContent()
                .build();
    }
}
