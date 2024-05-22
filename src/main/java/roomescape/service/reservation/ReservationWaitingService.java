package roomescape.service.reservation;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberRepository;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.reservation.ReservationWaiting;
import roomescape.domain.reservation.ReservationWaitingRepository;
import roomescape.domain.schedule.ReservationDate;
import roomescape.domain.schedule.ReservationTime;
import roomescape.domain.schedule.ReservationTimeRepository;
import roomescape.domain.schedule.Schedule;
import roomescape.domain.theme.Theme;
import roomescape.domain.theme.ThemeRepository;
import roomescape.exception.InvalidMemberException;
import roomescape.exception.InvalidReservationException;
import roomescape.exception.UnauthorizedException;
import roomescape.service.reservation.dto.ReservationRequest;
import roomescape.service.reservation.dto.ReservationWaitingResponse;

@Service
public class ReservationWaitingService {
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;
    private final ReservationWaitingRepository reservationWaitingRepository;
    private final ReservationRepository reservationRepository;

    public ReservationWaitingService(ReservationTimeRepository reservationTimeRepository,
                                     ThemeRepository themeRepository, MemberRepository memberRepository,
                                     ReservationWaitingRepository reservationWaitingRepository,
                                     ReservationRepository reservationRepository) {
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.memberRepository = memberRepository;
        this.reservationWaitingRepository = reservationWaitingRepository;
        this.reservationRepository = reservationRepository;
    }

    public ReservationWaitingResponse create(ReservationRequest waitingRequest, long memberId) {
        return createReservationWaiting(
                waitingRequest.timeId(), waitingRequest.themeId(), memberId, waitingRequest.date()
        );
    }

    public List<ReservationWaitingResponse> findAll() {
        return reservationWaitingRepository.findAll().stream()
                .map(ReservationWaitingResponse::new)
                .toList();
    }

    private ReservationWaitingResponse createReservationWaiting(
            long timeId, long themeId, long memberId, LocalDate date
    ) {
        ReservationDate reservationDate = ReservationDate.of(date);
        ReservationTime reservationTime = findTimeById(timeId);
        Schedule schedule = new Schedule(reservationDate, reservationTime);

        Theme theme = findThemeById(themeId);
        Member member = findMemberById(memberId);

        validate(reservationDate, reservationTime, theme, member, schedule);
        ReservationWaiting waiting = reservationWaitingRepository.save(new ReservationWaiting(member, theme, schedule));

        return new ReservationWaitingResponse(waiting);
    }

    private ReservationTime findTimeById(long timeId) {
        return reservationTimeRepository.findById(timeId)
                .orElseThrow(() -> new InvalidReservationException("더이상 존재하지 않는 시간입니다."));
    }

    private Theme findThemeById(long themeId) {
        return themeRepository.findById(themeId)
                .orElseThrow(() -> new InvalidReservationException("더이상 존재하지 않는 테마입니다."));
    }

    private Member findMemberById(long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new InvalidMemberException("존재하지 않는 회원입니다."));
    }

    private void validate(ReservationDate date, ReservationTime reservationTime, Theme theme, Member member, Schedule schedule) {
        if (!reservationRepository.existsByScheduleDateAndScheduleTimeIdAndThemeId(date, reservationTime.getId(), theme.getId())) {
            throw new InvalidReservationException("현재 해당 테마가 예약 가능하므로 예약 대기는 등록할 수 없습니다.");
        }
        if (reservationWaitingRepository.existsByMemberAndThemeAndSchedule(member, theme, schedule)) {
            throw new InvalidReservationException("이미 해당 테마에 예약 대기를 등록했습니다.");
        }
    }

    public void deleteById(long waitingId) {
        reservationWaitingRepository.deleteById(waitingId);
    }

    public void deleteById(long waitingId, long memberId) {
        reservationWaitingRepository.findById(waitingId)
                .ifPresent(waiting -> validateAuthority(waiting, memberId));
        deleteById(waitingId);
    }

    private void validateAuthority(ReservationWaiting waiting, long memberId) {
        if (!waiting.isWaitingOf(memberId)) {
            throw new UnauthorizedException("예약 대기를 취소할 권한이 없습니다.");
        }
    }
}
