package roomescape.waiting.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.member.domain.Member;
import roomescape.member.service.MemberService;
import roomescape.reservation.controller.response.ReservationResponse;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationDate;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.service.ThemeService;
import roomescape.time.domain.ReservationTime;
import roomescape.time.service.ReservationTimeService;
import roomescape.user.controller.dto.request.ReservationRequest;
import roomescape.user.controller.dto.response.MemberReservationResponse;
import roomescape.waiting.domain.Waiting;
import roomescape.waiting.repository.WaitingRepository;

@Service
public class WaitingService {

    private final WaitingRepository waitingRepository;
    private final MemberService memberService;
    private final ReservationTimeService reservationTimeService;
    private final ThemeService themeService;
    private final ReservationRepository reservationRepository;

    public WaitingService(WaitingRepository waitingRepository,
                          MemberService memberService, ReservationTimeService reservationTimeService,
                          ThemeService themeService, ReservationRepository reservationRepository) {
        this.waitingRepository = waitingRepository;
        this.memberService = memberService;
        this.reservationTimeService = reservationTimeService;
        this.themeService = themeService;
        this.reservationRepository = reservationRepository;
    }

    @Transactional
    public ReservationResponse createById(Long memberId, ReservationRequest request) {
        Member member = memberService.findById(memberId);
        ReservationDate reservationDate = new ReservationDate(request.date());
        Long timeId = request.timeId();
        ReservationTime reservationTime = reservationTimeService.getReservationTime(timeId);
        Long themeId = request.themeId();
        Theme theme = themeService.getTheme(themeId);

        validateAlreadyReservation(memberId, reservationDate, timeId, themeId);

        Waiting waiting = Waiting.create(reservationDate, reservationTime, theme, member);
        Waiting created = waitingRepository.save(waiting);

        return ReservationResponse.fromWaiting(created);
    }

    @Transactional(readOnly = true)
    public List<MemberReservationResponse> findAllByMemberId(Long id) {
        return waitingRepository.findAllWaitingWithRankByMemberId(id).stream()
                .map(MemberReservationResponse::fromWaitingWithRank)
                .toList();
    }

    @Transactional
    public void deleteById(Long id) {
        waitingRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("[ERROR] 대기를 찾을 수 없습니다."));
        waitingRepository.deleteById(id);
    }

    @Transactional
    public ReservationResponse approveWaitingById(Long id) {
        Waiting waiting = getWaiting(id);
        validateDuplicateReservation(waiting);
        waitingRepository.deleteById(id);
        Reservation reservation = waiting.toReservation();
        Reservation save = reservationRepository.save(reservation);
        return ReservationResponse.fromReservation(save);
    }

    @Transactional(readOnly = true)
    public List<MemberReservationResponse> findAllWithRank() {
        List<MemberReservationResponse> responses = new ArrayList<>(
                waitingRepository.findAllWithRank().stream()
                        .map(MemberReservationResponse::fromWaitingWithRank)
                        .toList()
        );

        responses.sort(
                Comparator
                        .comparing(MemberReservationResponse::date)
                        .thenComparing(r -> r.time().startAt())
                        .thenComparing(r -> r.theme().name())
        );

        return responses;
    }

    @Transactional(readOnly = true)
    public List<ReservationResponse> findAll() {
        List<Waiting> waitings = waitingRepository.findAll();
        return waitings.stream()
                .map(ReservationResponse::fromWaiting)
                .toList();
    }

    private void validateDuplicateReservation(Waiting waiting) {
        ReservationDate reservationDate = waiting.getReservationDate();
        Long timeId = waiting.getReservationTime().getId();
        Long themeId = waiting.getTheme().getId();
        if (reservationRepository.existsByReservationDateAndReservationTimeIdAndThemeId(reservationDate, timeId,
                themeId)) {
            throw new IllegalArgumentException("[ERROR] 현재 예약이 존재합니다. 취소 후 다시 요청해 주세요.");
        }
    }

    private Waiting getWaiting(Long id) {
        return waitingRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("[ERROR] 대기를 찾을 수 없습니다."));
    }

    private void validateAlreadyReservation(Long memberId, ReservationDate reservationDate, Long timeId, Long themeId) {
        if (reservationRepository.existsByReservationDateAndReservationTimeIdAndThemeIdAndMemberId(
                reservationDate, timeId, themeId, memberId
        )) {
            throw new IllegalArgumentException("[ERROR] 이미 예약을 한 회원입니다. 예약을 취소 후 대기를 신청해 주세요.");
        }
    }
}
