package roomescape.reservation.service;

import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.global.auth.dto.UserInfo;
import roomescape.member.domain.Member;
import roomescape.member.exception.MemberNotFoundException;
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.domain.ReservationInfo;
import roomescape.reservation.domain.Waiting;
import roomescape.reservation.dto.response.ReservationResponse;
import roomescape.reservation.dto.response.WaitingWithRank;
import roomescape.reservation.exception.ReservationNotFoundException;
import roomescape.reservation.exception.WaitingNotFoundException;
import roomescape.reservation.repository.WaitingReservationRepository;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.repository.ReservationTimeRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.repository.ThemeRepository;

@Service
public class WaitingModuleService {

    private final WaitingReservationRepository waitingReservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;
    private final ReservationModuleService reservationModuleService;

    public WaitingModuleService(final WaitingReservationRepository waitingReservationRepository,
                                final ReservationTimeRepository reservationTimeRepository,
                                final ThemeRepository themeRepository,
                                final MemberRepository memberRepository,
                                final ReservationModuleService reservationModuleService) {
        this.waitingReservationRepository = waitingReservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.memberRepository = memberRepository;
        this.reservationModuleService = reservationModuleService;
    }

    public List<Waiting> findMyWaitings(UserInfo userInfo) {
        return waitingReservationRepository.findByMemberId(userInfo.id());
    }


    private List<Waiting> getWaitingReservations(final Long themeId, final Long memberId,
                                                 final LocalDate startDate, final LocalDate endDate) {
        if ((themeId == null) || (memberId == null) || (startDate == null) || (endDate == null)) {
            return waitingReservationRepository.findAll();
        }
        return waitingReservationRepository.findFilteredReservations(themeId, memberId, startDate, endDate);
    }

    public void delete(Long id) {
        waitingReservationRepository.deleteById(id);
    }

    @Transactional
    public ReservationResponse create(final LocalDate date, final Long timeId, final Long themeId, final Long memberId,
                                      final LocalDateTime now) {
        if (!reservationModuleService.isReservationExists(date, timeId, themeId)) {
            return reservationModuleService.create(date, timeId, themeId, memberId, now);
        }
        ReservationTime time = findReservationTime(timeId);
        Theme theme = findTheme(themeId);
        Member member = findUserByMemberId(memberId);
        int turn = waitingReservationRepository.findMaxOrderByDateAndTimeAndTheme(date, timeId,
                themeId);
        Waiting newWaiting = waitingReservationRepository.save(
                Waiting.createUpcomingReservationWithUnassignedId(
                        member,
                        turn + 1,
                        new ReservationInfo(date, time, theme), now));
        return ReservationResponse.of(newWaiting, time, theme, member);
    }

    private Member findUserByMemberId(final Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException("멤버를 찾을 수 없습니다."));
    }

    private Theme findTheme(final Long request) {
        return themeRepository.findById(request)
                .orElseThrow(() -> new ReservationNotFoundException("요청한 id와 일치하는 테마 정보가 없습니다."));
    }

    private ReservationTime findReservationTime(final Long reservationTimeId) {
        return reservationTimeRepository.findById(reservationTimeId)
                .orElseThrow(() -> new ReservationNotFoundException("요청한 id와 일치하는 예약 시간 정보가 없습니다."));
    }

    public List<ReservationResponse> findWaitings() {
        return waitingReservationRepository.findAll()
                .stream()
                .map(reservation -> {
                    ReservationTime time = reservation.getInfo().getTime();
                    Theme theme = reservation.getInfo().getTheme();
                    Member member = reservation.getMember();
                    return ReservationResponse.of(reservation, time, theme, member);
                })
                .toList();
    }

    public List<WaitingWithRank> findMyWaitingsWithRank(UserInfo userInfo) {
        return waitingReservationRepository.findWaitingsWithRankByMemberId(userInfo.id());
    }

    public int findMaxOrderByDateAndTimeAndTheme(final LocalDate date, final Long timeId, final Long themeId) {
        return waitingReservationRepository.findMaxOrderByDateAndTimeAndTheme(date, timeId, themeId);
    }

    public Waiting save(final Waiting waiting) {
        return waitingReservationRepository.save(waiting);
    }

    public Waiting findFirstWaitingOfInfo(ReservationInfo reservationInfo) {
        return waitingReservationRepository.findFirstByInfoDateAndInfoTimeAndInfoThemeOrderByTurnAsc(reservationInfo.getDate(),
                        reservationInfo.getTime(), reservationInfo.getTheme())
                .orElseThrow(() -> new WaitingNotFoundException("요청한 id와 일치하는 대기 정보가 없습니다."));
    }

//
//    public List<MyWaitingReservationOutput> findMyWaitingReservations(final UserInfo userInfo) {
//        return reservationRepository.findByMemberId(userInfo.id())
//                .stream()
//                .map(MyWaitingReservationOutput::from)
//                .toList();
//    }
}
