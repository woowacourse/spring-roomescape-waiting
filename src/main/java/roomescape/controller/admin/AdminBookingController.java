package roomescape.controller.admin;

import java.time.LocalDate;
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
@RequestMapping("/admin/bookings")
public class AdminBookingController {

    private final BookingLookupService bookingLookupService;

    public AdminBookingController(BookingLookupService bookingLookupService) {
        this.bookingLookupService = bookingLookupService;
    }

    @GetMapping
    public ResponseEntity<List<BookingStatusResponse>> getBookings(
            @RequestParam("startDate") LocalDate startDate,
            @RequestParam("endDate") LocalDate endDate
    ) {
        List<BookingStatusResponse> results = bookingLookupService.findByDateRange(startDate, endDate)
                .stream().map(BookingStatusResponse::from).toList();
        return ResponseEntity.ok(results);
    }
}
