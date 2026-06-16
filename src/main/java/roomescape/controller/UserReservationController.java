package roomescape.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
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
import roomescape.controller.dto.MyReservationResponse;
import roomescape.controller.dto.ReservationRequest;
import roomescape.controller.dto.ReservationResponse;
import roomescape.controller.dto.ReservationUpdateRequest;
import roomescape.service.OrderService;
import roomescape.service.ReservationService;
import roomescape.service.dto.ReservationResult;

@Validated
@RestController
@RequestMapping("/user/reservations")
public class UserReservationController {
    private static final long DEFAULT_AMOUNT = 50_000L;

    private final ReservationService reservationService;
    private final OrderService orderService;

    public UserReservationController(ReservationService reservationService, OrderService orderService) {
        this.reservationService = reservationService;
        this.orderService = orderService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReservationResponse create(@RequestBody @Valid ReservationRequest request) {
        ReservationResult saved = reservationService.create(request.toCommand());
        orderService.create(DEFAULT_AMOUNT);
        return ReservationResponse.from(saved);
    }

    @GetMapping
    public List<MyReservationResponse> myList(@RequestParam @NotBlank(message = "이름은 비어 있을 수 없습니다.") String name) {
        return reservationService.findMyReservationsAndWaitings(name).stream()
                .map(MyReservationResponse::from)
                .toList();
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancel(@PathVariable Long id, @NotBlank(message = "이름은 비어 있을 수 없습니다.") @RequestParam String name) {
        reservationService.deleteByOwner(id, name);
    }

    @PatchMapping("/{id}")
    public ReservationResponse update(
            @PathVariable Long id,
            @RequestBody @Valid ReservationUpdateRequest request
    ) {
        ReservationResult updated = reservationService.updateByOwner(request.toCommand(id));
        return ReservationResponse.from(updated);
    }
}
