package roomescape.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.controller.request.ReservationRequest;
import roomescape.controller.response.MemberWaitingResponse;
import roomescape.controller.response.WaitingResponse;
import roomescape.model.Waiting;
import roomescape.model.member.LoginMember;
import roomescape.service.WaitingService;
import roomescape.service.dto.ReservationDto;

@RestController
@RequestMapping("/waitings")
public class WaitingController {

    private final WaitingService waitingService;

    public WaitingController(WaitingService waitingService) {
        this.waitingService = waitingService;
    }

    @PostMapping
    public ResponseEntity<WaitingResponse> addWaiting(@Valid @RequestBody ReservationRequest request,
                                                      LoginMember member) {
        ReservationDto reservationDto = ReservationDto.of(request, member);
        Waiting waiting = waitingService.saveWaiting(reservationDto);
        WaitingResponse response = new WaitingResponse(waiting);
        return ResponseEntity
                .created(URI.create("/waitings/" + response.getId()))
                .body(response);
    }

    @GetMapping("/mine")
    public ResponseEntity<List<MemberWaitingResponse>> getReservationsOfMember(LoginMember member) {
        List<Waiting> waitings = waitingService.findWaitingsByMember(member);
        List<MemberWaitingResponse> response = waitings.stream()
                .map((MemberWaitingResponse::new))
                .toList();
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReservation(@Min(1) @PathVariable("id") Long id) {
        waitingService.deleteWaiting(id);
        return ResponseEntity.noContent().build();
    }
}
