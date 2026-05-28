package roomescape.controller.member;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import roomescape.controller.dto.request.ReservationRequest;
import roomescape.controller.dto.response.ReservationDetailResponses;
import roomescape.controller.dto.response.ReservationResponse;
import roomescape.service.ReservationService;
import roomescape.service.dto.command.ReservationCommand;
import roomescape.service.dto.result.ReservationDetailResults;
import roomescape.service.dto.result.ReservationResult;

@RestController
@RequestMapping("/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping
    public ResponseEntity<List<ReservationResponse>> read() {
        List<ReservationResponse> response = reservationService.findAll().stream()
                .map(ReservationResponse::from)
                .toList();

        return ResponseEntity.ok().body(response);
    }

    @GetMapping(params = "userName")
    public ResponseEntity<ReservationDetailResponses> readAllByUserName(
            @RequestParam("userName") String userName) {
        ReservationDetailResults result = reservationService.findAllByUserName(userName);

        ReservationDetailResponses response = ReservationDetailResponses.from(result);

        return ResponseEntity.ok().body(response);
    }

    @PostMapping
    public ResponseEntity<ReservationResponse> create(@Valid @RequestBody ReservationRequest request) {
        ReservationResult result = reservationService.save(ReservationCommand.from(request));
        ReservationResponse response = ReservationResponse.from(result);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReservationResponse> update(@PathVariable Long id,
                                                      @Valid @RequestBody ReservationRequest request) {
        ReservationResult result = reservationService.updateDateTime(id, ReservationCommand.from(request));
        ReservationResponse response = ReservationResponse.from(result);

        return ResponseEntity.ok().body(response);
    }

    @DeleteMapping(value = "/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, @RequestParam("userName") String userName) {
        reservationService.delete(id, userName);
        return ResponseEntity.noContent().build();
    }
}