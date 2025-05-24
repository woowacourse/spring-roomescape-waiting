package roomescape.controller;

import jakarta.validation.Valid;
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
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import roomescape.annotation.CheckRole;
import roomescape.dto.request.CreateReservationRequest;
import roomescape.dto.request.CreateWaitReservationRequest;
import roomescape.dto.request.LoginMemberRequest;
import roomescape.dto.response.MyReservationResponse;
import roomescape.dto.response.ReservationResponse;
import roomescape.dto.response.ReservationWaitResponse;
import roomescape.global.Role;
import roomescape.service.ReservationService;

@RestController
@RequestMapping("/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping
    @CheckRole(Role.ADMIN)
    public ResponseEntity<List<ReservationResponse>> getReservations() {
        List<ReservationResponse> responses = reservationService.findAll();

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/mine")
    @CheckRole({Role.USER, Role.ADMIN})
    public ResponseEntity<List<MyReservationResponse>> getMyReservation(LoginMemberRequest loginMemberRequest) {
        List<MyReservationResponse> responses = reservationService.findAllReservationOfMember(
                loginMemberRequest.id());

        return ResponseEntity.ok(responses);
    }

    @PostMapping
    @CheckRole(value = {Role.ADMIN, Role.USER})
    public ResponseEntity<ReservationResponse> addReservations(
            @RequestBody @Valid CreateReservationRequest request,
            LoginMemberRequest loginMemberRequest) {
        ReservationResponse response = reservationService.addReservation(request, loginMemberRequest);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }

    @PostMapping("/waiting")
    @CheckRole(value = {Role.ADMIN, Role.USER})
    public ResponseEntity<ReservationWaitResponse> addWaitReservation(
            @RequestBody @Valid CreateWaitReservationRequest request,
            LoginMemberRequest loginMemberRequest) {
        ReservationWaitResponse response = reservationService.addWaitReservation(request, loginMemberRequest);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }

    @DeleteMapping("/{id}")
    @CheckRole(value = {Role.ADMIN, Role.USER})
    public ResponseEntity<Void> deleteReservations(@PathVariable Long id) {
        reservationService.deleteReservation(id);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/waiting/{id}")
    @CheckRole(value = {Role.ADMIN, Role.USER})
    public ResponseEntity<Void> deleteWaitReservation(@PathVariable Long id) {
        reservationService.deleteReservation(id);

        return ResponseEntity.noContent().build();
    }
}
