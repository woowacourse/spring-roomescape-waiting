package roomescape.adapter.web;

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
import roomescape.adapter.payment.TossProperties;
import roomescape.adapter.web.dto.request.ReservationRequest;
import roomescape.adapter.web.dto.request.ReservationUpdateRequest;
import roomescape.adapter.web.dto.response.MyReservationResponse;
import roomescape.adapter.web.dto.response.PaymentReadyResponse;
import roomescape.adapter.web.dto.response.ReservationResponse;
import roomescape.application.ReservationService;
import roomescape.application.dto.result.ReservationOrderResult;
import roomescape.application.dto.result.ReservationResult;

@Validated
@RestController
@RequestMapping("/user/reservations")
public class UserReservationController {

    private final ReservationService reservationService;
    private final TossProperties tossProperties;

    public UserReservationController(ReservationService reservationService,
                                     TossProperties tossProperties) {
        this.reservationService = reservationService;
        this.tossProperties = tossProperties;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PaymentReadyResponse create(@RequestBody @Valid ReservationRequest request) {
        ReservationOrderResult result = reservationService.reserveWithPayment(request.toCommand());
        return PaymentReadyResponse.of(result, tossProperties.clientKey());
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
