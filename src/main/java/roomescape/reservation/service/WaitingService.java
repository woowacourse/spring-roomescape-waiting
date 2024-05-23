package roomescape.reservation.service;

import org.springframework.stereotype.Service;
import roomescape.member.domain.Member;
import roomescape.member.dto.LoginMemberInToken;
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.Theme;
import roomescape.reservation.domain.Waiting;
import roomescape.reservation.dto.WaitingCreateRequest;
import roomescape.reservation.dto.WaitingResponse;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.repository.ReservationTimeRepository;
import roomescape.reservation.repository.ThemeRepository;
import roomescape.reservation.repository.WaitingRepository;

@Service
public class WaitingService {

    private final WaitingRepository waitingRepository;
    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;

    public WaitingService(
            WaitingRepository waitingRepository,
            ReservationRepository reservationRepository,
            ReservationTimeRepository reservationTimeRepository,
            ThemeRepository themeRepository,
            MemberRepository memberRepository
    ) {
        this.waitingRepository = waitingRepository;
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.memberRepository = memberRepository;
    }

    public Long save(WaitingCreateRequest WaitingCreateRequest, LoginMemberInToken loginMemberInToken) {
        Waiting waiting = getValidatedWaiting(WaitingCreateRequest, loginMemberInToken);

        return waitingRepository.save(waiting).getId();
    }

    private Waiting getValidatedWaiting(WaitingCreateRequest waitingCreateRequest,
            LoginMemberInToken loginMemberInToken) {
        ReservationTime reservationTime = reservationTimeRepository.findById(waitingCreateRequest.timeId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 예약 시간입니다."));

        Theme theme = themeRepository.findById(waitingCreateRequest.themeId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 테마입니다."));

        Member member = memberRepository.findById(loginMemberInToken.id())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        final Waiting waiting = waitingCreateRequest.toWaiting(member, theme, reservationTime);
        validateWaiting(waiting);
        return waiting;
    }

    private void validateWaiting(Waiting waiting) {
        Reservation reservation = reservationRepository.findByThemeIdAndDateAndReservationTimeStartAt(
                        waiting.getTheme().getId(), waiting.getDate(), waiting.getReservationTime().getStartAt())
                .orElseThrow(() -> new IllegalArgumentException("예약이 없어 예약 대기를 할 수 없습니다."));

        final Long memberId = waiting.getMember().getId();
        if (memberId.equals(reservation.getId())) {
            throw new IllegalArgumentException("이미 예약 중 입니다.");
        }

        if (waitingRepository.existsByThemeIdAndDateAndReservationTimeStartAt(
                waiting.getTheme().getId(), waiting.getDate(), waiting.getReservationTime().getStartAt())) {
            throw new IllegalArgumentException("이미 예약 대기 중 입니다.");
        }
    }

    public WaitingResponse findById(Long id) {
        Waiting waiting = waitingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 예약입니다."));

        return new WaitingResponse(waiting);
    }

//    public List<ReservationResponse> findAll() {
//        return reservationRepository.findAll().stream()
//                .map(ReservationResponse::new)
//                .toList();
//    }
//
//    public List<ReservationResponse> findAllBySearch(ReservationSearchRequest reservationSearchRequest) {
//        Member member = memberRepository.findById(reservationSearchRequest.memberId()).get();
//        Theme theme = themeRepository.findById(reservationSearchRequest.themeId()).get();
//
//        return reservationRepository.findAllByMemberAndThemeAndDateBetween(member,
//                        theme,
//                        reservationSearchRequest.dateFrom(), reservationSearchRequest.dateTo()).stream()
//                .map(ReservationResponse::new)
//                .toList();
//    }
//
//    public List<MyReservationResponse> findAllByMemberId(Long memberId) {
//        return reservationRepository.findAllByMemberId(memberId).stream()
//                .map(MyReservationResponse::new)
//                .toList();
//    }
//
//    public void delete(Long id) {
//        reservationRepository.deleteById(id);
//    }
}
