package roomescape.presentation.rest;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import roomescape.application.ReservationService;
import roomescape.application.UserService;
import roomescape.presentation.request.CreateReservationAdminRequest;
import roomescape.presentation.response.ReservationResponse;
import roomescape.presentation.response.UserResponse;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final ReservationService reservationService;
    private final UserService userService;

    public AdminController(final ReservationService reservationService, final UserService userService) {
        this.reservationService = reservationService;
        this.userService = userService;
    }

    @PostMapping("/reservations")
    public ResponseEntity<ReservationResponse> reserve(@RequestBody @Valid final CreateReservationAdminRequest request) {
        var user = userService.getById(request.userId());
        var reservation = reservationService.reserve(user, request.date(), request.timeId(), request.themeId());
        var response = ReservationResponse.from(reservation);
        return ResponseEntity.created(URI.create("reservations/" + reservation.id())).body(response);
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        var users = userService.findAllUsers();
        var response = UserResponse.from(users);
        return ResponseEntity.ok(response);
    }
}
