package roomescape.web.controller;

import java.net.URI;
import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import roomescape.service.ReservationWaitService;
import roomescape.service.dto.request.member.MemberInfo;
import roomescape.service.dto.request.wait.WaitRequest;
import roomescape.service.dto.response.wait.WaitResponse;

@RestController
@RequiredArgsConstructor
public class WaitController {
    private final ReservationWaitService waitService;

    @GetMapping("/reservations-mine")
    public ResponseEntity<List<WaitResponse>> findAllByMemberId(MemberInfo memberInfo) {
        List<WaitResponse> reservations = waitService.findAllByMemberId(memberInfo.id());

        return ResponseEntity.ok(reservations);
    }

    @PostMapping("/reservation-wait")
    public ResponseEntity<Void> saveReservationWait(@Valid @RequestBody WaitRequest request, MemberInfo memberInfo) {
        waitService.saveReservationWait(request, memberInfo.id());

        return ResponseEntity.created(URI.create("/")).build();
    }

    @DeleteMapping("/reservation-wait/{reservationId}")
    public ResponseEntity<Void> deleteReservation(@PathVariable("reservationId") Long reservationId,
                                                  MemberInfo memberInfo) {
        waitService.deleteReservationWait(reservationId, memberInfo.id());

        return ResponseEntity.noContent().build();
    }
}
