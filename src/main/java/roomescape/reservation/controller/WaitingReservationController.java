package roomescape.reservation.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.dto.LoginMember;
import roomescape.reservation.dto.WaitingReservationRequest;
import roomescape.reservation.dto.WaitingReservationResponse;
import roomescape.reservation.service.WaitingReservationService;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/waitingReservations")

public class WaitingReservationController {

    private final WaitingReservationService waitingReservationService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public WaitingReservationResponse save(@Valid @RequestBody final WaitingReservationRequest request,
                                           final LoginMember member) {
        return waitingReservationService.save(request, member);
    }

    @DeleteMapping("{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@NotNull @PathVariable final Long id) {
        waitingReservationService.deleteById(id);
    }
}
