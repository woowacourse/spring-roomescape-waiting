package roomescape.reservation.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.exception.BadArgumentRequestException;
import roomescape.member.domain.Member;
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationSearch;
import roomescape.reservation.dto.MyReservationResponse;
import roomescape.reservation.dto.ReservationCreateRequest;
import roomescape.reservation.dto.ReservationResponse;
import roomescape.reservation.dto.ReservationSearchRequest;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.repository.ThemeRepository;
import roomescape.time.domain.ReservationTime;
import roomescape.time.repository.TimeRepository;
import roomescape.waiting.domain.Waiting;
import roomescape.waiting.repository.WaitingRepository;

@Service
public class ReservationService {
    private final ReservationRepository reservationRepository;
    private final WaitingRepository waitingRepository;
    private final MemberRepository memberRepository;
    private final TimeRepository timeRepository;
    private final ThemeRepository themeRepository;

    public ReservationService(ReservationRepository reservationRepository,
                              WaitingRepository waitingRepository,
                              MemberRepository memberRepository,
                              TimeRepository timeRepository,
                              ThemeRepository themeRepository) {
        this.reservationRepository = reservationRepository;
        this.waitingRepository = waitingRepository;
        this.memberRepository = memberRepository;
        this.timeRepository = timeRepository;
        this.themeRepository = themeRepository;
    }

    public List<ReservationResponse> findReservations() {
        return reservationRepository.findAll()
                .stream()
                .map(ReservationResponse::from)
                .toList();
    }

    public List<ReservationResponse> findReservations(ReservationSearchRequest request) {
        ReservationSearch search = request.createReservationSearch();
        return reservationRepository.findByCondition(search.memberId(), search.themeId(), search.startDate(), search.endDate())
                .stream()
                .map(ReservationResponse::from)
                .toList();
    }

    public List<MyReservationResponse> findMyReservations(Long memberId) {
        List<MyReservationResponse> reservations = findReservationsByMemberId(memberId);
        List<MyReservationResponse> waitings = findWaitingsByMemberId(memberId);

        List<MyReservationResponse> response = new ArrayList<>();
        response.addAll(reservations);
        response.addAll(waitings);
        response.sort(Comparator.comparing(MyReservationResponse::date).thenComparing(MyReservationResponse::startAt));
        return response;
    }

    private List<MyReservationResponse> findReservationsByMemberId(Long memberId) {
        return reservationRepository.findByMemberId(memberId)
                .stream()
                .map(MyReservationResponse::from)
                .toList();
    }

    private List<MyReservationResponse> findWaitingsByMemberId(Long memberId) {
        return waitingRepository.findByMemberId(memberId)
                .stream()
                .map(waiting -> MyReservationResponse.from(waiting, countOrderOfWaiting(waiting)))
                .toList();
    }

    private Long countOrderOfWaiting(Waiting waiting) {
        return waitingRepository.countByReservationAndCreatedAtLessThanEqual(
                waiting.getReservation(), waiting.getCreatedAt());
    }

    public ReservationResponse createReservation(ReservationCreateRequest request) {
        Member member = findMemberByMemberId(request.memberId());
        ReservationTime time = findTimeByTimeId(request.timeId());
        Theme theme = findThemeByThemeId(request.themeId());
        Reservation reservation = request.createReservation(member, time, theme);

        return createReservation(reservation);
    }

    public ReservationResponse createReservation(ReservationCreateRequest request, Long memberId) {
        Member member = findMemberByMemberId(memberId);
        ReservationTime time = findTimeByTimeId(request.timeId());
        Theme theme = findThemeByThemeId(request.themeId());
        Reservation reservation = request.createReservation(member, time, theme);

        return createReservation(reservation);
    }

    private ReservationResponse createReservation(Reservation reservation) {
        validateIsAvailable(reservation);
        Reservation createdReservation = reservationRepository.save(reservation);
        return ReservationResponse.from(createdReservation);
    }

    private Member findMemberByMemberId(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new BadArgumentRequestException("해당 멤버가 존재하지 않습니다."));
    }

    private ReservationTime findTimeByTimeId(Long timeId) {
        return timeRepository.findById(timeId)
                .orElseThrow(() -> new BadArgumentRequestException("해당 예약 시간이 존재하지 않습니다."));
    }

    private Theme findThemeByThemeId(Long themeId) {
        return themeRepository.findById(themeId)
                .orElseThrow(() -> new BadArgumentRequestException("해당 테마가 존재하지 않습니다."));
    }

    private void validateIsAvailable(Reservation reservation) {
        if (reservation.isBefore(LocalDateTime.now())) {
            throw new BadArgumentRequestException("예약은 현재 시간 이후여야 합니다.");
        }
    }

    public void deleteReservation(Long id) {
        reservationRepository.deleteById(id);
    }
}
