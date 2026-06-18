package roomescape.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.controller.dto.request.ReservationCreateRequest;
import roomescape.controller.dto.response.ReservationWaitListResponse;
import roomescape.controller.dto.response.ReservationWaitResponse;
import roomescape.facade.ReservationFacade;

@RestController
@RequestMapping(value = "/reservations")
public class ReservationController {

    private final ReservationFacade reservationFacade;

    public ReservationController(ReservationFacade reservationFacade) {
        this.reservationFacade = reservationFacade;
    }

    @PostMapping
    public ResponseEntity<ReservationWaitResponse> save(
            @RequestHeader("Member-Id") Long memberId,
            @RequestBody ReservationCreateRequest request) {
        ReservationWaitResponse response = reservationFacade.save(request, memberId);
        return ResponseEntity.
                status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/mine")
    public ResponseEntity<ReservationWaitListResponse> findByMemberId(
            @RequestHeader("Member-Id") Long memberId
    ) {
        ReservationWaitListResponse response = reservationFacade.findByMemberId(memberId);
        return ResponseEntity.ok(response);
    }

    @GetMapping(params = "name")
    public ResponseEntity<ReservationWaitListResponse> findByName(
            @RequestParam("name") String name
    ) {
        ReservationWaitListResponse response = reservationFacade.findByName(name);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<ReservationWaitListResponse> findAll() {
        ReservationWaitListResponse response = reservationFacade.findAll();
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReservation(@PathVariable Long id) {
        reservationFacade.deleteReservation(id);
        return ResponseEntity
                .noContent()
                .build();
    }

    @DeleteMapping("/waits/{id}")
    public ResponseEntity<Void> deleteWait(@PathVariable Long id) {
        reservationFacade.deleteWait(id);
        return ResponseEntity
                .noContent()
                .build();
    }
}
