package roomescape.presentation.controller;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.application.ReservationService;
import roomescape.application.auth.dto.MemberIdDto;
import roomescape.application.dto.ReservationCreateServiceRequest;
import roomescape.application.dto.ReservationServiceResponse;
import roomescape.application.dto.ReservationStatusServiceResponse;
import roomescape.infrastructure.AuthenticatedMemberId;
import roomescape.presentation.controller.dto.ReservationStatusResponse;
import roomescape.presentation.controller.dto.UserReservationCreateRequest;

@RestController
@RequestMapping("/reservations")
public class ReservationController {

    private final ReservationService service;

    public ReservationController(ReservationService service) {
        this.service = service;
    }

    @GetMapping
    public List<ReservationServiceResponse> getAllReservations() {
        return service.getAllReservations();
    }

    @GetMapping("/mine")
    public List<ReservationStatusResponse> getMemberReservations(@AuthenticatedMemberId MemberIdDto memberIdDto) {
        List<ReservationStatusServiceResponse> reservationStatuses = service.getReservationsByMember(memberIdDto.id());
        return reservationStatuses.stream()
                .map(ReservationStatusResponse::from)
                .toList();
    }

    @PostMapping
    public ResponseEntity<ReservationServiceResponse> createReservation(
            @Valid @RequestBody UserReservationCreateRequest request,
            @AuthenticatedMemberId MemberIdDto memberIdDto
    ) {
        ReservationCreateServiceRequest reservationRequest = ReservationCreateServiceRequest.of(
                request,
                memberIdDto.id()
        );
        ReservationServiceResponse reservationServiceResponse = service.registerReservation(reservationRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(reservationServiceResponse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReservation(@PathVariable("id") Long id) {
        service.deleteReservation(id);
        return ResponseEntity.noContent().build();
    }
}
