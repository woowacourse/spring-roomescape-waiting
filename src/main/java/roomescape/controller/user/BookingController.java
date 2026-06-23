package roomescape.controller.user;

import jakarta.validation.constraints.NotBlank;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.controller.dto.response.BookingStatusResponse;
import roomescape.service.BookingLookupService;

@Validated
@RestController
@RequestMapping("/bookings")
public class BookingController {

    private final BookingLookupService bookingLookupService;

    public BookingController(BookingLookupService bookingLookupService) {
        this.bookingLookupService = bookingLookupService;
    }

    @GetMapping
    public ResponseEntity<List<BookingStatusResponse>> getBookings(
            @RequestParam("name") @NotBlank(message = "name은 비어 있을 수 없습니다.") String name
    ) {
        List<BookingStatusResponse> results = bookingLookupService.findByName(name)
                .stream().map(BookingStatusResponse::from).toList();
        return ResponseEntity.ok(results);
    }
}
