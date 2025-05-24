package roomescape.booking;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.AuthenticationPrincipal;
import roomescape.auth.dto.LoginMember;
import roomescape.booking.dto.BookingResponse;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/bookings")
public class BookingController {

    private final BookingService bookingService;

    @GetMapping
    public ResponseEntity<List<BookingResponse>> readAllByMember(
            @AuthenticationPrincipal LoginMember member
    ) {
        List<BookingResponse> bookings = bookingService.readAllByMember(member);
        return ResponseEntity.ok().body(bookings);
    }
}
