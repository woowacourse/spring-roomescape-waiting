package roomescape.application.reservation;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.application.reservation.dto.CreateWaitingParam;
import roomescape.application.reservation.dto.WaitingResult;
import roomescape.application.reservation.dto.WaitingWitStatusResult;
import roomescape.application.support.exception.NotFoundEntityException;
import roomescape.domain.BusinessRuleViolationException;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberRepository;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.ReservationTimeRepository;
import roomescape.domain.reservation.Theme;
import roomescape.domain.reservation.ThemeRepository;
import roomescape.domain.reservation.ThemeSchedule;
import roomescape.domain.reservation.Waiting;
import roomescape.domain.reservation.WaitingRank;
import roomescape.domain.reservation.WaitingRepository;

@Service
public class WaitingService {

    private final WaitingRepository waitingRepository;
    private final ReservationRepository reservationRepository;
    private final MemberRepository memberRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final Clock clock;

    public WaitingService(WaitingRepository waitingRepository,
                          ReservationRepository reservationRepository,
                          MemberRepository memberRepository,
                          ReservationTimeRepository reservationTimeRepository,
                          ThemeRepository themeRepository,
                          Clock clock) {
        this.waitingRepository = waitingRepository;
        this.reservationRepository = reservationRepository;
        this.memberRepository = memberRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.clock = clock;
    }

    public void create(CreateWaitingParam waitingParam) {
        ReservationTime reservationTime = getReservationTimeById(waitingParam.timeId());
        Member member = getMemberById(waitingParam.memberId());
        Theme theme = getThemeById(waitingParam.themeId());
        ThemeSchedule themeSchedule = new ThemeSchedule(waitingParam.date(), reservationTime, theme);

        validateCreateWaiting(themeSchedule, member);
        Waiting waiting = Waiting.create(
                LocalDateTime.now(clock),
                themeSchedule,
                member
        );
        waitingRepository.save(waiting);
    }

    public List<WaitingWitStatusResult> findWaitingRanks(Long memberId) {
        Member member = getMemberById(memberId);
        List<WaitingRank> waitingRanks = waitingRepository.findWaitingRankByMember(member);
        return waitingRanks.stream()
                .map(WaitingWitStatusResult::from)
                .toList();
    }

    public void delete(Long waitingId) {
        waitingRepository.deleteById(waitingId);
    }

    public List<WaitingResult> findAllWaitings() {
        List<Waiting> waitings = waitingRepository.findAllWithMemberAndThemeAndTime();
        return waitings.stream()
                .map(WaitingResult::from)
                .toList();
    }

    private void validateCreateWaiting(ThemeSchedule themeSchedule, Member member) {
        if (!reservationRepository.existsByThemeSchedule(themeSchedule)) {
            throw new BusinessRuleViolationException("예약이 바로 가능해 예약 대기를 할 수 없습니다.");
        }
        if (reservationRepository.existsByThemeScheduleAndMemberId(themeSchedule, member.getId())) {
            throw new BusinessRuleViolationException("이미 예약중입니다.");
        }
        if (waitingRepository.existsByThemeScheduleAndMemberId(themeSchedule, member.getId())) {
            throw new BusinessRuleViolationException("이미 예약 대기 중입니다.");
        }
    }

    private Member getMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundEntityException(memberId + "에 해당하는 member 튜플이 없습니다."));
    }

    private ReservationTime getReservationTimeById(Long timeId) {
        return reservationTimeRepository.findById(timeId)
                .orElseThrow(() -> new NotFoundEntityException(timeId + "에 해당하는 reservation_time 튜플이 없습니다."));
    }

    private Theme getThemeById(Long themeId) {
        return themeRepository.findById(themeId)
                .orElseThrow(() -> new NotFoundEntityException(themeId + "에 해당하는 theme 튜플이 없습니다."));
    }
}
