package roomescape.web.controller;

import java.net.URI;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import roomescape.service.ReservationWaitService;
import roomescape.service.dto.request.member.MemberInfo;
import roomescape.service.dto.request.wait.WaitRequest;

@RestController
@RequiredArgsConstructor
@RequestMapping("/reservation-wait")
public class WaitController {
    private final ReservationWaitService waitService;

    @PostMapping
    public ResponseEntity<Void> saveReservationWait(@Valid @RequestBody WaitRequest request, MemberInfo memberInfo) {
        waitService.saveReservationWait(request, memberInfo.id());

        return ResponseEntity.created(URI.create("/"))
                .build();
    }

    @DeleteMapping("/{waitId}")
    public ResponseEntity<Void> deleteReservation(@PathVariable("waitId") Long waitId) {

        return ResponseEntity.noContent().build();
    }
}
