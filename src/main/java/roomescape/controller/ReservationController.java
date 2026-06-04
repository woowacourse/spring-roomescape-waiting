package roomescape.controller;

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
import roomescape.controller.dto.response.ControllerReceptionListResponse;
import roomescape.controller.dto.response.ControllerReceptionResponse;
import roomescape.facade.ReceptionFacade;
import roomescape.service.dto.response.ServiceReceptionListResponse;
import roomescape.service.dto.response.ServiceReceptionResponse;

@RestController
@RequestMapping(value = "/reservations")
public class ReservationController {

    private final ReceptionFacade receptionFacade;

    public ReservationController(ReceptionFacade receptionFacade) {
        this.receptionFacade = receptionFacade;
    }

    @PostMapping
    public ResponseEntity<ControllerReceptionResponse> save(
            @RequestBody ControllerReservationCreateRequest request) {
        ServiceReceptionResponse serviceResponse = receptionFacade.save(
                request.toServiceReservationRequest());
        ControllerReceptionResponse controllerResponse = ControllerReceptionResponse.from(serviceResponse);
        return ResponseEntity.
                status(HttpStatus.CREATED)
                .body(controllerResponse);
    }

    @GetMapping(params = "name")
    public ResponseEntity<ControllerReceptionListResponse> findByName(
            @RequestParam("name") String name
    ) {
        ServiceReceptionListResponse serviceResponse = receptionFacade.findByName(name);
        ControllerReceptionListResponse controllerResponse = ControllerReceptionListResponse.from(serviceResponse);
        return ResponseEntity.ok(controllerResponse);
    }

    @GetMapping
    public ResponseEntity<ControllerReceptionListResponse> findAll() {
        ServiceReceptionListResponse serviceResponse = receptionFacade.findAll();
        ControllerReceptionListResponse controllerResponse = ControllerReceptionListResponse.from(serviceResponse);
        return ResponseEntity.ok(controllerResponse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReservation(@PathVariable Long id) {
        receptionFacade.deleteReservation(id);
        return ResponseEntity
                .noContent()
                .build();
    }

    @DeleteMapping("/waits/{id}")
    public ResponseEntity<Void> deleteWait(@PathVariable Long id) {
        receptionFacade.deleteWait(id);
        return ResponseEntity
                .noContent()
                .build();
    }
}
