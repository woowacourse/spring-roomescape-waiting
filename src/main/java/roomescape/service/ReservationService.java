package roomescape.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.member.Member;
import roomescape.domain.repository.*;
import roomescape.domain.reservation.*;
import roomescape.exception.customexception.RoomEscapeBusinessException;
import roomescape.service.dto.request.ReservationConditionRequest;
import roomescape.service.dto.request.ReservationSaveRequest;
import roomescape.service.dto.response.ReservationResponse;
import roomescape.service.dto.response.ReservationResponses;
import roomescape.service.dto.response.UserReservationResponse;
import roomescape.service.dto.response.UserReservationResponses;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ReservationService {
    private final WaitingRepository waitingRepository;
    private final ReservationRepository reservationRepository;
    private final MemberRepository memberRepository;
    private final ThemeRepository themeRepository;
    private final ReservationTimeRepository timeRepository;

    public ReservationService(
            WaitingRepository waitingRepository,
            ReservationRepository reservationRepository,
            MemberRepository memberRepository,
            ThemeRepository themeRepository,
            ReservationTimeRepository timeRepository
    ) {
        this.waitingRepository = waitingRepository;
        this.reservationRepository = reservationRepository;
        this.memberRepository = memberRepository;
        this.themeRepository = themeRepository;
        this.timeRepository = timeRepository;
    }

    public ReservationResponse saveReservation(ReservationSaveRequest reservationSaveRequest) {
        Reservation reservation = createReservation(reservationSaveRequest);
        validateUnique(reservation);
        Reservation savedReservation = reservationRepository.save(reservation);
        return new ReservationResponse(savedReservation);
    }

    private void validateUnique(Reservation reservation) {
        if (reservationRepository.existsByReservationSlot(reservation.getReservationSlot())) {
            throw new RoomEscapeBusinessException("이미 존재하는 예약입니다.");
        }
    }

    public void deleteReservation(long id) {
        Reservation reservation = findReservation(id);
        List<Waiting> waitings = waitingRepository.findByReservationOrderById(reservation);

        if (!waitings.isEmpty()) {
            acceptWaiting(waitings, reservation);
            return;
        }

        reservationRepository.delete(reservation);
    }

    private Reservation findReservation(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new RoomEscapeBusinessException("예약이 존재하지 않습니다."));
    }

    private void acceptWaiting(List<Waiting> waitings, Reservation reservation) {
        Waiting nextWaiting = Collections.min(waitings);
        Member member = nextWaiting.getMember();
        reservation.setMember(member);
        reservationRepository.save(reservation);
        waitingRepository.delete(nextWaiting);
    }

    public ReservationResponses findReservationsByCondition(ReservationConditionRequest reservationConditionRequest) {
        List<Reservation> reservations = findByConditions(reservationConditionRequest);
        return toReservationResponses(reservations);
    }

    private List<Reservation> findByConditions(ReservationConditionRequest reservationConditionRequest) {
        return reservationRepository.findByConditions(
                Optional.ofNullable(reservationConditionRequest.dateFrom()),
                Optional.ofNullable(reservationConditionRequest.dateTo()),
                reservationConditionRequest.themeId(),
                reservationConditionRequest.memberId()
        );
    }

    private ReservationResponses toReservationResponses(List<Reservation> reservations) {
        List<ReservationResponse> reservationResponses = reservations.stream()
                .map(ReservationResponse::new)
                .toList();
        return new ReservationResponses(reservationResponses);
    }

    public UserReservationResponses findAllUserReservation(Long memberId) {
        Member user = findMemberById(memberId);
        return getAllReservationAndWaiting(user);
    }

    private UserReservationResponses getAllReservationAndWaiting(Member user) {
        List<UserReservationResponse> allReservations = new ArrayList<>();
        allReservations.addAll(findUserReservations(user));
        allReservations.addAll(findUserWaitings(user));
        return new UserReservationResponses(allReservations);
    }

    private List<UserReservationResponse> findUserReservations(Member user) {
        return reservationRepository.findByMember(user)
                .stream()
                .map(UserReservationResponse::reserved)
                .toList();
    }

    private List<UserReservationResponse> findUserWaitings(Member user) {
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

    public Reservation createReservation(ReservationSaveRequest request) {
        ReservationSlot slot = new ReservationSlot(
                request.date(),
                findTimeById(request.timeId()),
                findThemeById(request.themeId())
        );

        return new Reservation(findMemberById(request.memberId()), slot);
    }

    private Member findMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new RoomEscapeBusinessException("회원이 존재하지 않습니다."));
    }

    private Theme findThemeById(Long id) {
        return themeRepository.findById(id)
                .orElseThrow(() -> new RoomEscapeBusinessException("존재하지 않는 테마입니다."));
    }

    private ReservationTime findTimeById(Long id) {
        return timeRepository.findById(id)
                .orElseThrow(() -> new RoomEscapeBusinessException("존재하지 않는 예약 시간입니다."));
    }
}

