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
import roomescape.controller.dto.response.ReceptionResponse;
import roomescape.facade.ReceptionFacade;

@RestController
@RequestMapping(value = "/reservations")
public class ReservationController {

    private final ReceptionFacade receptionFacade;

    public ReservationController(ReceptionFacade receptionFacade) {
        this.receptionFacade = receptionFacade;
    }

    @PostMapping
    public ResponseEntity<ReceptionResponse> save(
            @RequestBody ControllerReservationCreateRequest request) {
        ReceptionResponse response = receptionFacade.save(request.toServiceReservationRequest());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping(params = "name")
    public ResponseEntity<List<ReceptionResponse>> findByName(
            @RequestParam("name") String name
    ) {
        return ResponseEntity.ok(receptionFacade.findByName(name));
    }

    @GetMapping
    public ResponseEntity<List<ReceptionResponse>> findAll() {
        return ResponseEntity.ok(receptionFacade.findAll());
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
