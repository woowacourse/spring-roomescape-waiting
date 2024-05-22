package roomescape.reservation.controller;

import java.net.URI;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.dto.LoggedInMember;
import roomescape.reservation.dto.MyReservationResponse;
import roomescape.reservation.dto.ReservationCreateRequest;
import roomescape.reservation.dto.ReservationResponse;
import roomescape.reservation.service.ReservationDeleteUsecase;
import roomescape.reservation.service.ReservationFindMineUsecase;
import roomescape.reservation.service.ReservationFindService;
import roomescape.reservation.service.ReservationService;

@RestController
@RequestMapping("/reservations")
public class ReservationController {
    private final ReservationFindService findService;
    private final ReservationService service;
    private final ReservationDeleteUsecase deleteUsecase;
    private final ReservationFindMineUsecase findMineUsecase;

    public ReservationController(ReservationFindService findService,
                                 ReservationService service,
                                 ReservationDeleteUsecase deleteUsecase,
                                 ReservationFindMineUsecase findMineUsecase) {
        this.findService = findService;
        this.service = service;
        this.deleteUsecase = deleteUsecase;
        this.findMineUsecase = findMineUsecase;
    }

    @GetMapping
    public List<ReservationResponse> findReservations() {
        return findService.findReservations();
    }

    @GetMapping("/accounts")
    public List<MyReservationResponse> findMyReservations(LoggedInMember member) {
        return findMineUsecase.findMyReservations(member.id());
    }

    @PostMapping
    public ResponseEntity<ReservationResponse> createReservation(
            @RequestBody ReservationCreateRequest request,
            LoggedInMember member) {
        ReservationResponse response = service.createReservation(request, member.id());

        URI location = URI.create("/reservations/" + response.id());
        return ResponseEntity.created(location)
                .body(response);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteReservation(@PathVariable Long id) {
        deleteUsecase.deleteReservation(id);
    }
}
