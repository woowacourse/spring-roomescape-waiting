package roomescape.reservation.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import roomescape.member.dto.MemberProfileInfo;
import roomescape.reservation.dto.MyReservationResponse;
import roomescape.reservation.dto.ReservationConditionSearchRequest;
import roomescape.reservation.dto.ReservationCreateRequest;
import roomescape.reservation.dto.ReservationRequest;
import roomescape.reservation.dto.ReservationResponse;
import roomescape.reservation.dto.ReservationTimeAvailabilityResponse;

@Service
public class ReservationFacadeService {
    private final ReservationDetailService reservationDetailService;
    private final ReservationService reservationService;
    private final ReservationWaitingService waitingService;

    public ReservationFacadeService(ReservationDetailService reservationDetailService,
                                    ReservationService reservationService,
                                    ReservationWaitingService waitingService) {
        this.reservationDetailService = reservationDetailService;
        this.reservationService = reservationService;
        this.waitingService = waitingService;
    }

    public List<ReservationResponse> findReservations() {
        return reservationService.findReservations();
    }

    public List<ReservationResponse> findReservationWaitings() {
        return waitingService.findReservationWaitings();
    }

    public List<MyReservationResponse> findReservationsByMember(MemberProfileInfo memberProfileInfo) {
        List<MyReservationResponse> reservationResponse = reservationService.findReservationByMemberId(memberProfileInfo.id());
        List<MyReservationResponse> waitingResponse = waitingService.findReservationWaitingByMemberId(memberProfileInfo.id());

        List<MyReservationResponse> response = new ArrayList<>();
        response.addAll(reservationResponse);
        response.addAll(waitingResponse);

        return response;
    }

    public List<ReservationTimeAvailabilityResponse> findReservationTimes(long themeId, LocalDate date) {
        return reservationService.findTimeAvailability(themeId, date);
    }

    public List<ReservationResponse> findReservationsInCondition(ReservationConditionSearchRequest request) {
        return reservationService.findReservationsByConditions(request);
    }

    public ReservationResponse createReservation(ReservationCreateRequest request) {
        Long detailId = reservationDetailService.findReservationDetailId(request);
        ReservationRequest reservationRequest = new ReservationRequest(request.memberId(), detailId);
        return reservationService.addReservation(reservationRequest);
    }

    public ReservationResponse createWaitingReservation(ReservationCreateRequest request) {
        Long detailId = reservationDetailService.findReservationDetailId(request);
        ReservationRequest reservationRequest = new ReservationRequest(request.memberId(), detailId);

        waitingService.findReservationWaitingByDetailId(reservationRequest);
        return waitingService.addReservationWaiting(reservationRequest);
    }

    public void deleteReservation(long id) {
        ReservationRequest reservation = reservationService.findReservation(id);
        reservationService.deleteReservation(id);

        Optional<ReservationRequest> newReservation = waitingService.findFirstByDetailId(reservation.detailId());
        newReservation.ifPresent(reservationService::addReservation);
    }

    public void deleteReservationWaiting(long id) {
        waitingService.removeReservations(id);
    }
}
