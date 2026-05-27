package roomescape.controller;

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
import roomescape.controller.dto.request.ControllerReservationCreateRequest;
import roomescape.controller.dto.response.ControllerReservationResponse;
import roomescape.service.ReceptionFacade;
import roomescape.service.dto.response.ServiceReceptionResponse;

@RestController
@RequestMapping(value = "/reservations")
public class ReservationController {

    private final ReceptionFacade receptionFacade;

    public ReservationController(ReceptionFacade receptionFacade) {
        this.receptionFacade = receptionFacade;
    }

    @PostMapping
    public ResponseEntity<ControllerReservationResponse> create(
            @RequestBody ControllerReservationCreateRequest request) {
        ServiceReceptionResponse serviceResponses = receptionFacade.create(
                request.toServiceReservationRequest());
        ControllerReservationResponse controllerResponse = ControllerReservationResponse.from(serviceResponses);
        return ResponseEntity.
                status(HttpStatus.CREATED)
                .body(controllerResponse);
    }

    @GetMapping(params = "name")
    public ResponseEntity<List<ControllerReservationResponse>> readByName(
            @RequestParam("name") String name
    ) {
        List<ServiceReceptionResponse> serviceResponses = receptionFacade.readByName(name);
        List<ControllerReservationResponse> controllerResponses = serviceResponses.stream()
                .map(ControllerReservationResponse::from)
                .toList();
        return ResponseEntity.ok(controllerResponses);
    }

    @GetMapping
    public ResponseEntity<List<ControllerReservationResponse>> readAll() {
        List<ServiceReceptionResponse> serviceResponses = receptionFacade.readAll();
        List<ControllerReservationResponse> controllerResponse = serviceResponses.stream()
                .map(ControllerReservationResponse::from)
                .toList();
        return ResponseEntity.ok(controllerResponse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReservation(@PathVariable Long id) {
        receptionFacade.deleteReservation(id);
        return ResponseEntity
                .noContent()
                .build();
    }

    @DeleteMapping("/wait/{id}")
    public ResponseEntity<Void> deleteWait(@PathVariable Long id) {
        receptionFacade.deleteWait(id);
        return ResponseEntity
                .noContent()
                .build();
    }
}
