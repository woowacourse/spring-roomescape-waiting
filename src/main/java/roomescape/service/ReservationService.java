package roomescape.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.member.Member;
import roomescape.domain.repository.ReservationRepository;
import roomescape.domain.repository.WaitingRepository;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.Waiting;
import roomescape.exception.customexception.RoomEscapeBusinessException;
import roomescape.service.dbservice.ReservationDbService;
import roomescape.service.dto.request.ReservationConditionRequest;
import roomescape.service.dto.request.ReservationSaveRequest;
import roomescape.service.dto.response.ReservationResponse;
import roomescape.service.dto.response.ReservationResponses;
import roomescape.service.dto.response.UserReservationResponse;
import roomescape.service.dto.response.UserReservationResponses;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ReservationService {
    private final ReservationDbService reservationDbService;
    private final WaitingRepository waitingRepository;
    private final ReservationRepository reservationRepository;

    public ReservationService(ReservationDbService reservationDbService, WaitingRepository waitingRepository, ReservationRepository reservationRepository) {
        this.reservationDbService = reservationDbService;
        this.waitingRepository = waitingRepository;
        this.reservationRepository = reservationRepository;
    }

    public ReservationResponse saveReservation(ReservationSaveRequest reservationSaveRequest) {
        Reservation reservation = reservationDbService.createReservation(
                reservationSaveRequest.memberId(),
                reservationSaveRequest.date(),
                reservationSaveRequest.timeId(),
                reservationSaveRequest.themeId()
        );

        validateUnique(reservation);

        Reservation savedReservation = reservationDbService.save(reservation);
        return new ReservationResponse(savedReservation);
    }

    private void validateUnique(Reservation reservation) {
        if (reservationDbService.hasReservation(reservation)) {
            throw new RoomEscapeBusinessException("이미 존재하는 예약입니다.");
        }
    }

    public void deleteReservation(Long id) {
        Reservation reservation = reservationDbService.findById(id);
        List<Waiting> waitings = waitingRepository.findByReservationOrderById(reservation);

        if (!waitings.isEmpty()) {
            acceptWaiting(waitings.get(0), reservation);
            return;
        }

        reservationRepository.delete(reservation);
    }

    private void acceptWaiting(Waiting waiting, Reservation reservation) {
        Member member = waiting.getMember();
        reservation.setMember(member);
        reservationRepository.save(reservation);
        waitingRepository.delete(waiting);
    }

    public ReservationResponses findReservationsByCondition(ReservationConditionRequest reservationConditionRequest) {
        List<Reservation> reservations = reservationDbService.findByConditions(
                Optional.ofNullable(reservationConditionRequest.dateFrom()),
                Optional.ofNullable(reservationConditionRequest.dateTo()),
                reservationConditionRequest.themeId(),
                reservationConditionRequest.memberId()
        );

        return toReservationResponses(reservations);
    }

    private ReservationResponses toReservationResponses(List<Reservation> reservations) {
        List<ReservationResponse> reservationResponses = reservations.stream()
                .map(ReservationResponse::new)
                .toList();
        return new ReservationResponses(reservationResponses);
    }

    public UserReservationResponses findAllUserReservation(Long memberId) {
        Member user = reservationDbService.findMemberById(memberId);
        return getAllUserReservations(user);
    }

    private UserReservationResponses getAllUserReservations(Member user) {
        List<UserReservationResponse> allReservations = new ArrayList<>();
        allReservations.addAll(findUserReservations(user));
        allReservations.addAll(findUserWaitingReservations(user));
        return new UserReservationResponses(allReservations);
    }

    private List<UserReservationResponse> findUserReservations(Member user) {
        return reservationDbService.findMemberReservations(user)
                .stream()
                .map(UserReservationResponse::reserved)
                .toList();
    }

    private List<UserReservationResponse> findUserWaitingReservations(Member user) {
        return waitingRepository.findByMember(user)
                .stream()
                .map(waiting -> UserReservationResponse.from(waiting, getWaitingRank(waiting)))
                .toList();
    }

    // TODO 성능 생각해서 쿼리 줄이기
    private int getWaitingRank(Waiting waiting) {
        List<Waiting> waitings = waitingRepository.findByReservationOrderById(waiting.getReservation());
        return waitings.indexOf(waiting);
    }
}

