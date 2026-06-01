package roomescape.reservation.service;

import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.dto.request.ReservationRequest;
import roomescape.reservation.dto.request.ReservationTimeCreateRequest;
import roomescape.reservation.dto.request.UpdateMyReservation;
import roomescape.reservation.dto.response.MyReservationResponse;
import roomescape.reservation.dto.response.ReservationCreateResponse;
import roomescape.reservation.dto.response.ReservationResponse;
import roomescape.reservation.dto.response.ReservationTimeCreateResponse;
import roomescape.reservation.dto.response.ReservationTimeFindAllResponse;
import roomescape.theme.domain.Theme;
import roomescape.theme.service.ThemeService;

@Service
public class ReservationFacade {

    private final ReservationService reservationService;
    private final ReservationTimeService reservationTimeService;
    private final ThemeService themeService;

    public ReservationFacade(ReservationService reservationService, ReservationTimeService reservationTimeService, ThemeService themeService) {
        this.reservationService = reservationService;
        this.reservationTimeService = reservationTimeService;
        this.themeService = themeService;
    }

    public ReservationCreateResponse createReservation(ReservationRequest request) {
        ReservationTime time = reservationTimeService.findById(request.timeId());
        Theme theme = themeService.findById(request.themeId());
        return reservationService.create(request, time, theme);
    }

    public void deleteReservationTime(Long id) {
        if (reservationService.existsByTimeId(id)) {
            throw new IllegalStateException("[ERROR] 해당 시간에 예약이 존재하여 삭제할 수 없습니다.");
        }

        reservationTimeService.delete(id);
    }

    public ReservationTimeCreateResponse createReservationTime(ReservationTimeCreateRequest reservationTimeCreateRequest) {
        return reservationTimeService.create(reservationTimeCreateRequest);
    }

    public List<ReservationTimeFindAllResponse> findAllReservationTime() {
        return reservationTimeService.findAll();
    }

    public List<ReservationResponse> findAllReservation() {
        return reservationService.findAll();
    }

    public void deleteReservation(Long id) {
        reservationService.delete(id);
    }

    public ReservationResponse findReservationById(Long id) {
        return reservationService.findById(id);
    }

    public void deleteReservationByNameAndReservationId(String name, Long reservationId) {
        reservationService.deleteByNameAndReservationId(name, reservationId);
    }

    public void updateMyReservation(UpdateMyReservation updateMyReservation, String name, Long reservationId) {
        reservationService.updateMyReservation(updateMyReservation, name, reservationId);
    }

    public List<MyReservationResponse> findReservationsByName(String name) {
        return reservationService.findAllByName(name);
    }
}
