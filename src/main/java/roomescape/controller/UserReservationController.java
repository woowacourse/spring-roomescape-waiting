package roomescape.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import roomescape.controller.dto.ReservationRequest;
import roomescape.controller.dto.ReservationResponse;
import roomescape.controller.dto.ReservationUpdateRequest;
import roomescape.service.UserReservationService;
import roomescape.service.dto.ReservationResult;

@RestController
@RequestMapping("/user/reservations")
@Validated
public class UserReservationController {

    private final UserReservationService userReservationService;

    public UserReservationController(UserReservationService userReservationService) {
        this.userReservationService = userReservationService;
    }

    @GetMapping
    public List<ReservationResponse> list(
            @RequestParam @NotBlank(message = "reserverName은 필수입니다.") String reserverName
    ) {
        return userReservationService.findByReserverName(reserverName).stream()
                .map(ReservationResponse::from)
                .toList();
    }

    @PostMapping
    public ReservationResponse create(@RequestBody @Valid ReservationRequest request) {
        ReservationResult saved = userReservationService.create(request.toCommand());
        return ReservationResponse.from(saved);
    }

    @PatchMapping("/{id}")
    public ReservationResponse update(
            @PathVariable @Positive(message = "id는 0보다 커야합니다.") Long id,
            @RequestBody @Valid ReservationUpdateRequest request
    ) {
        ReservationResult updated = userReservationService.update(request.toCommand(id));
        return ReservationResponse.from(updated);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancel(
            @PathVariable @Positive(message = "id는 0보다 커야합니다.") Long id,
            @RequestParam @NotBlank(message = "reserverName은 필수입니다.") String reserverName
    ) {
        userReservationService.cancel(id, reserverName);
    }
}
