package roomescape.reservationtime.application;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.bookingslot.domain.service.BookingSlotDomainService;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.domain.service.ReservationTimeDomainService;
import roomescape.reservationtime.presentation.dto.request.ReservationTimeCreateRequest;
import roomescape.reservationtime.presentation.dto.response.AvailableReservationTimeResponse;
import roomescape.reservationtime.presentation.dto.response.ReservationTimeResponse;

@Service
public class ReservationTimeApplicationService {

    private final ReservationTimeDomainService reservationTimeDomainService;
    private final BookingSlotDomainService bookingSlotDomainService;

    public ReservationTimeApplicationService(final ReservationTimeDomainService reservationTimeDomainService,
                                             final BookingSlotDomainService bookingSlotDomainService) {
        this.reservationTimeDomainService = reservationTimeDomainService;
        this.bookingSlotDomainService = bookingSlotDomainService;
    }

    public List<ReservationTimeResponse> getReservationTimes() {
        return reservationTimeDomainService.findAll().stream()
                .map(ReservationTimeResponse::from)
                .toList();
    }

    public void delete(Long id) {
        reservationTimeDomainService.delete(id);
    }

    public ReservationTimeResponse create(final ReservationTimeCreateRequest request) {
        ReservationTime newReservationTime = reservationTimeDomainService.save(request.toReservationTime());
        return ReservationTimeResponse.from(newReservationTime);
    }

    public List<AvailableReservationTimeResponse> getAvailableReservationTimes(final LocalDate date,
                                                                               final Long themeId) {
        return bookingSlotDomainService.findBookedTimesByDateAndThemeId(date, themeId);
    }
}
