package roomescape.controller;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.controller.dto.request.ControllerReservationCreateRequest;
import roomescape.controller.dto.request.ControllerReservationUpdateRequest;
import roomescape.controller.dto.response.ControllerReservationResponse;
import roomescape.service.ReservationService;
import roomescape.service.dto.response.ServiceReservationResponse;

@RestController
@RequestMapping(value = "/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping
    public ResponseEntity<ControllerReservationResponse> create(
            @Valid @RequestBody ControllerReservationCreateRequest request) {
        ServiceReservationResponse serviceResponse = reservationService.create(
                request.toServiceReservationRequest());
        ControllerReservationResponse controllerResponse = ControllerReservationResponse.from(serviceResponse);
        return ResponseEntity.
                status(HttpStatus.CREATED)
                .body(controllerResponse);
    }

    @GetMapping(params = "name")
    public ResponseEntity<List<ControllerReservationResponse>> readByName(
            @RequestParam("name") String name
    ) {
        List<ServiceReservationResponse> serviceResponses = reservationService.readByName(name);
        List<ControllerReservationResponse> controllerResponses = serviceResponses.stream()
                .map(ControllerReservationResponse::from)
                .toList();
        return ResponseEntity.ok(controllerResponses);
    }

    @GetMapping
    public ResponseEntity<List<ControllerReservationResponse>> readAll() {
        List<ServiceReservationResponse> serviceResponses = reservationService.readAll();
        List<ControllerReservationResponse> controllerResponse = serviceResponses.stream()
                .map(ControllerReservationResponse::from)
                .toList();
        return ResponseEntity.ok(controllerResponse);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ControllerReservationResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody ControllerReservationUpdateRequest request) {
        ServiceReservationResponse serviceResponse = reservationService.update(id,
                request.toServiceReservationRequest());
        ControllerReservationResponse controllerResponse = ControllerReservationResponse.from(serviceResponse);

        return ResponseEntity.ok(controllerResponse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        reservationService.delete(id);
        return ResponseEntity
                .noContent()
                .build();
    }
}
