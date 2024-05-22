package roomescape.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.*;
import roomescape.domain.repository.MemberRepository;
import roomescape.domain.repository.ReservationRepository;
import roomescape.domain.repository.ReservationTimeRepository;
import roomescape.domain.repository.ThemeRepository;
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
    private final MemberRepository memberRepository;
    private final ReservationDbService reservationDbService;

    public ReservationService(
            MemberRepository memberRepository,
            ReservationDbService reservationDbService
    ) {
        this.memberRepository = memberRepository;
        this.reservationDbService = reservationDbService;
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
        reservationDbService.delete(id);
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
        Member user = findMemberById(memberId);
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
                .map(reservation -> new UserReservationResponse(reservation.getId(), reservation, user))
                .toList();
    }

    private List<UserReservationResponse> findUserWaitingReservations(Member user) {
        return user.getWaitings().stream()
                .map(waiting -> new UserReservationResponse(waiting.getId(), waiting.getReservation(), user))
                .toList();
    }

    private Member findMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new RoomEscapeBusinessException("회원이 존재하지 않습니다."));
    }
}

