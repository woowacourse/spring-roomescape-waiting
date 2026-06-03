package roomescape.controller;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import roomescape.dto.request.MyReservationRequest;
import roomescape.dto.request.ReservationCancelRequest;
import roomescape.dto.request.ReservationRequest;
import roomescape.dto.request.ReservationUpdateRequest;
import roomescape.dto.request.ReservationUpdateMemberRequest;
import roomescape.dto.response.ReservationResponse;
import roomescape.service.MyReservationQueryService;
import roomescape.service.ReservationCommandService;

@RestController
@RequestMapping("/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationCommandService reservationCommandService;
    private final MyReservationQueryService myReservationQueryService;

    @GetMapping
    public ResponseEntity<List<ReservationResponse>> getMyReservations(@Valid @ModelAttribute MyReservationRequest request) {
        return ResponseEntity.ok(myReservationQueryService.getMyReservations(request.name()));
    }

    @PostMapping
    public ResponseEntity<ReservationResponse> createReservation(@Valid @RequestBody ReservationRequest request) {
        ReservationResponse reservationResponse = ReservationResponse.from(
                reservationCommandService.create(request.name(), request.date(), request.timeId(), request.themeId()));

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(reservationResponse.id())
                .toUri();

        return ResponseEntity.created(location).body(reservationResponse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelReservation(@PathVariable long id, @Valid @ModelAttribute ReservationCancelRequest member) {
        reservationCommandService.cancel(id, member.name());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ReservationResponse> updateReservation(
            @PathVariable long id,
            @Valid @ModelAttribute ReservationUpdateMemberRequest member,
            @Valid @RequestBody ReservationUpdateRequest request) {
        ReservationResponse response = ReservationResponse.from(
                reservationCommandService.update(id, member.name(), request.date(), request.timeId()));
        return ResponseEntity.ok(response);
    }
}
