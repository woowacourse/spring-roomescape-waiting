package roomescape.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.dto.query.WaitingWithRank;
import roomescape.dto.request.AdminCreateReservationRequest;
import roomescape.dto.request.CreateReservationRequest;
import roomescape.dto.request.CreateWaitingRequest;
import roomescape.dto.request.LoginMemberRequest;
import roomescape.entity.Reservation;
import roomescape.entity.Waiting;

@Service
public class ReservationFacade {
    private final ReservationService reservationService;
    private final WaitingService waitingService;

    public ReservationFacade(ReservationService reservationService, WaitingService waitingService) {
        this.reservationService = reservationService;
        this.waitingService = waitingService;
    }

    public Reservation addReservation(CreateReservationRequest request, LoginMemberRequest loginMemberRequest) {
        return reservationService.addReservation(request, loginMemberRequest);
    }

    public Reservation addReservationByAdmin(AdminCreateReservationRequest request) {
        return reservationService.addReservationByAdmin(request);
    }

    public List<Reservation> findAllReservation() {
        return reservationService.findAll();
    }

    public List<Reservation> findAllReservationByFilter(
            Long memberId,
            Long themeId,
            LocalDate dateFrom,
            LocalDate dateTo
    ) {
        return reservationService.findAllByFilter(memberId, themeId, dateFrom, dateTo);
    }

    public void deleteReservation(Long id) {
        reservationService.deleteReservation(id);
    }

    public List<Reservation> findAllReservationByMember(final Long memberId) {
        return reservationService.findAllReservationByMember(memberId);
    }

    public Waiting addWaiting(CreateWaitingRequest request, LoginMemberRequest loginMemberRequest) {
        return waitingService.addWaiting(request, loginMemberRequest);
    }

    public List<WaitingWithRank> findALlWaitingWithRank(Long memberId){
        return waitingService.findALlWaitingWithRank(memberId);
    }

}
