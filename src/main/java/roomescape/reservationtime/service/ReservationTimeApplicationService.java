package roomescape.reservationtime.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.reservation.service.ReservationDomainService;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.dto.request.ReservationTimeCreateRequest;
import roomescape.reservationtime.dto.response.AvailableReservationTimeResponse;
import roomescape.reservationtime.dto.response.ReservationTimeResponse;
import roomescape.reservationtime.exception.ReservationTimeAlreadyExistsException;
import roomescape.reservationtime.exception.ReservationTimeInUseException;

@Service
public class ReservationTimeApplicationService {

    private final ReservationTimeDomainService reservationTimeDomainService;
    private final ReservationDomainService reservationDomainService;

    public ReservationTimeApplicationService(final ReservationTimeDomainService reservationTimeDomainService,
                                             final ReservationDomainService reservationDomainService) {
        this.reservationTimeDomainService = reservationTimeDomainService;
        this.reservationDomainService = reservationDomainService;
    }

    public List<ReservationTimeResponse> getReservationTimes() {
        return reservationTimeDomainService.getReservationTimes();
    }

    public void delete(Long id) {
        if (reservationDomainService.existsByTimeId(id)) {
            throw new ReservationTimeInUseException("해당 시간에 대한 예약이 존재하여 삭제할 수 없습니다.");
        }
        reservationTimeDomainService.deleteById(id);
    }

    public ReservationTimeResponse create(final ReservationTimeCreateRequest request) {
        validateIsTimeUnique(request);
        ReservationTime newReservationTime = reservationTimeDomainService.save(request.toReservationTime());
        return ReservationTimeResponse.from(newReservationTime);
    }

    public List<AvailableReservationTimeResponse> getAvailableReservationTimes(final LocalDate date,
                                                                               final Long themeId) {
        return reservationDomainService.findBookedTimesByDateAndThemeId(date, themeId);
    }

    public ReservationTime findReservationTime(final Long reservationTimeId) {
        return reservationTimeDomainService.findReservationTime(reservationTimeId);
    }

    private void validateIsTimeUnique(final ReservationTimeCreateRequest request) {
        if (reservationTimeDomainService.existsByStartAt(request.startAt())) {
            throw new ReservationTimeAlreadyExistsException("중복된 예약 시간을 생성할 수 없습니다.");
        }
    }
}
