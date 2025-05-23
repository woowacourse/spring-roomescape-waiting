package roomescape.controller;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.ArrayList;
import java.util.Comparator;
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
import roomescape.dto.query.WaitingWithRank;
import roomescape.dto.request.CreateReservationRequest;
import roomescape.dto.request.LoginMemberRequest;
import roomescape.dto.response.MyReservationResponse;
import roomescape.dto.response.ReservationResponse;
import roomescape.entity.ConfirmedReservation;
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
        List<ConfirmedReservation> reservations = reservationService.findAllConfirmReservation();
        List<ReservationResponse> responses = reservations.stream()
                .map(ReservationResponse::from)
                .toList();

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/mine")
    @CheckRole({Role.USER, Role.ADMIN})
    public ResponseEntity<List<MyReservationResponse>> getMyReservation(LoginMemberRequest loginMemberRequest) {
        List<ConfirmedReservation> reservations = reservationService.findAllReservationByMember(loginMemberRequest.id());

        List<WaitingWithRank> allWaitingWithRank = reservationService.findALlWaitingWithRank(loginMemberRequest.id());

        List<MyReservationResponse> reservationResponses = reservations.stream()
                .map(MyReservationResponse::from)
                .toList();

        List<MyReservationResponse> waitingResponses = allWaitingWithRank.stream()
                .map(MyReservationResponse::from)
                .toList();

        List<MyReservationResponse> combinedResponses = new ArrayList<>(reservationResponses);
        combinedResponses.addAll(waitingResponses);
        combinedResponses.sort(Comparator.comparing(MyReservationResponse::date));

        return ResponseEntity.ok(combinedResponses);
    }

    @PostMapping
    @CheckRole(value = {Role.ADMIN, Role.USER})
    public ResponseEntity<ReservationResponse> addReservations(
            @RequestBody @Valid CreateReservationRequest request,
            LoginMemberRequest loginMemberRequest
    ) {
        ConfirmedReservation reservation = reservationService.addReservation(request, loginMemberRequest);
        ReservationResponse response = ReservationResponse.from(reservation);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }

    @DeleteMapping("/{id}")
    @CheckRole(value = {Role.ADMIN, Role.USER})
    public ResponseEntity<Void> deleteReservations(@PathVariable("id") Long id) {
        reservationService.deleteReservation(id);

        return ResponseEntity.noContent().build();
    }
}
