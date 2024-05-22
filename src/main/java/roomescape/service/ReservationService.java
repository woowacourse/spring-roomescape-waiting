package roomescape.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.Waiting;
import roomescape.domain.WaitingWithRank;
import roomescape.handler.exception.CustomException;
import roomescape.handler.exception.ExceptionCode;
import roomescape.repository.MemberRepository;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;
import roomescape.repository.WaitingRepository;
import roomescape.service.dto.request.ReservationConditionRequest;
import roomescape.service.dto.request.ReservationRequest;
import roomescape.service.dto.request.UserWaitingRequest;
import roomescape.service.dto.response.MyReservationResponse;
import roomescape.service.dto.response.ReservationResponse;
import roomescape.service.dto.response.WaitingResponse;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;
    private final WaitingRepository waitingRepository;

    public ReservationService(ReservationRepository reservationRepository,
                              ReservationTimeRepository reservationTimeRepository,
                              ThemeRepository themeRepository,
                              MemberRepository memberRepository, WaitingRepository waitingRepository
    ) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.memberRepository = memberRepository;
        this.waitingRepository = waitingRepository;
    }

    public ReservationResponse createReservation(ReservationRequest reservationRequest, Long memberId) {
        Member member = getMember(memberId);
        ReservationTime reservationTime = getReservationTime(reservationRequest.timeId());
        Theme theme = getTheme(reservationRequest.themeId());

        if (reservationRepository.existsByTimeAndDate(reservationTime, reservationRequest.date())) {
            throw new CustomException(ExceptionCode.DUPLICATE_RESERVATION);
        }
        validateIsPastTime(reservationRequest.date(), reservationTime);

        Reservation reservation = reservationRequest.toEntity(member, reservationTime, theme);
        Reservation savedReservation = reservationRepository.save(reservation);
        return ReservationResponse.from(savedReservation);
    }

    public WaitingResponse createReservationWaiting(UserWaitingRequest userWaitingRequest, Long memberId) {
        Member member = getMember(memberId);
        ReservationTime reservationTime = getReservationTime(userWaitingRequest.timeId());
        Theme theme = getTheme(userWaitingRequest.themeId());

        validateIsPastTime(userWaitingRequest.date(), reservationTime);

        Waiting waiting = userWaitingRequest.toEntity(member, reservationTime, theme);
        Waiting savedReservation = waitingRepository.save(waiting);
        return WaitingResponse.from(savedReservation);
    }

    private void validateIsPastTime(LocalDate date, ReservationTime time) {
        LocalDateTime reservationDateTime = LocalDateTime.of(date, time.getStartAt());
        if (LocalDateTime.now().isAfter(reservationDateTime)) {
            throw new CustomException(ExceptionCode.PAST_TIME_SLOT_RESERVATION);
        }
    }

    public List<ReservationResponse> findAllReservations() {
        List<Reservation> reservations = reservationRepository.findAll();
        return reservations.stream()
                .map(ReservationResponse::from)
                .toList();
    }

    public List<ReservationResponse> findAllReservationsByCondition(ReservationConditionRequest condition) {
        Theme findTheme = getTheme(condition.themeId());
        Member findMember = getMember(condition.memberId());
        List<Reservation> reservations = reservationRepository.findAllByThemeAndMemberAndDateBetween(
                findTheme, findMember, condition.dateFrom(), condition.dateTo());

        return reservations.stream()
                .map(ReservationResponse::from)
                .toList();
    }

    public List<MyReservationResponse> findAllByMemberId(Long id) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));
        List<Reservation> reservations = reservationRepository.findAllByMember(member);

        List<MyReservationResponse> reservationResponses = new java.util.ArrayList<>(reservations.stream()
                .map(MyReservationResponse::from)
                .toList());

        List<WaitingWithRank> waitingWithRanks = waitingRepository.findWaitingsWithRankByMemberId(id);
        List<MyReservationResponse> waitingResponses = waitingWithRanks.stream()
                .map(MyReservationResponse::from)
                .toList();

        reservationResponses.addAll(waitingResponses);
        return reservationResponses;
    }

    public List<WaitingResponse> findAllWaitings() {
        List<Waiting> waitings = waitingRepository.findAll();
        return waitings.stream()
                .map(WaitingResponse::from)
                .toList();
    }

    public void deleteReservation(Long id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new CustomException(ExceptionCode.NOT_FOUND_RESERVATION));

        Optional<Waiting> waiting = waitingRepository.findFirstByDateAndThemeAndTime(

                reservation.getDate(), reservation.getTheme(),
                reservation.getTime());
        if (waiting.isPresent()) {
        waiting.ifPresent(waitingRepository::delete);
        reservationRepository.save(waiting.get().toReservation());
        }
        reservationRepository.deleteById(id);
    }

    public void deleteWaiting(Long id) {
        waitingRepository.deleteById(id);
    }

    private Theme getTheme(Long id) {
        return themeRepository.findById(id)
                .orElseThrow(() -> new CustomException(ExceptionCode.NOT_FOUND_THEME));
    }

    private ReservationTime getReservationTime(Long id) {
        return reservationTimeRepository.findById(id)
                .orElseThrow(() -> new CustomException(ExceptionCode.NOT_FOUND_RESERVATION_TIME));
    }

    private Member getMember(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new CustomException(ExceptionCode.NOT_FOUND_USER));
    }
}
