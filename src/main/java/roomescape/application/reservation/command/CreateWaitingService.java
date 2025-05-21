package roomescape.application.reservation.command;

import java.time.Clock;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.application.reservation.command.dto.CreateWaitingCommand;
import roomescape.domain.member.Member;
import roomescape.domain.member.repository.MemberRepository;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.Theme;
import roomescape.domain.reservation.Waiting;
import roomescape.domain.reservation.repository.ReservationRepository;
import roomescape.domain.reservation.repository.ReservationTimeRepository;
import roomescape.domain.reservation.repository.ThemeRepository;
import roomescape.domain.reservation.repository.WaitingRepository;
import roomescape.infrastructure.error.exception.MemberException;
import roomescape.infrastructure.error.exception.ReservationTimeException;
import roomescape.infrastructure.error.exception.ThemeException;
import roomescape.infrastructure.error.exception.WaitingException;

@Service
@Transactional
public class CreateWaitingService {

    private final WaitingRepository waitingRepository;
    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;
    private final Clock clock;

    public CreateWaitingService(WaitingRepository waitingRepository,
                                ReservationRepository reservationRepository,
                                ReservationTimeRepository reservationTimeRepository,
                                ThemeRepository themeRepository,
                                MemberRepository memberRepository,
                                Clock clock) {
        this.waitingRepository = waitingRepository;
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.memberRepository = memberRepository;
        this.clock = clock;
    }

    public Long request(CreateWaitingCommand createCommand) {
        validateNotAlreadyReservedOrWaiting(createCommand);
        validateReservationExists(createCommand);
        Member member = getMember(createCommand.memberId());
        ReservationTime time = getTime(createCommand.timeId());
        Theme theme = getTheme(createCommand.themeId());
        Waiting waiting = new Waiting(member, createCommand.reservationDate(), time, theme);
        waiting.validateWaitable(LocalDateTime.now(clock));
        Waiting savedWaiting = waitingRepository.save(waiting);
        return savedWaiting.getId();
    }

    private void validateNotAlreadyReservedOrWaiting(CreateWaitingCommand createCommand) {
        if (isAlreadyReservedOrWaiting(createCommand)) {
            throw new WaitingException("이미 예약했거나 대기 중입니다.");
        }
    }

    private boolean isAlreadyReservedOrWaiting(CreateWaitingCommand createCommand) {
        boolean isAlreadyReserved = reservationRepository.existsByDateAndTimeIdAndThemeIdAndMemberId(
                createCommand.reservationDate(),
                createCommand.timeId(),
                createCommand.themeId(),
                createCommand.memberId()
        );
        boolean isAlreadyWaiting = waitingRepository.existsByDateAndTimeIdAndThemeIdAndMemberId(
                createCommand.reservationDate(),
                createCommand.timeId(),
                createCommand.themeId(),
                createCommand.memberId()
        );
        return isAlreadyReserved || isAlreadyWaiting;
    }

    private void validateReservationExists(CreateWaitingCommand createCommand) {
        boolean exists = reservationRepository.existsByDateAndTimeIdAndThemeId(
                createCommand.reservationDate(),
                createCommand.timeId(),
                createCommand.themeId()
        );
        if (!exists) {
            throw new WaitingException("예약이 존재하지 않아 대기를 신청할 수 없습니다. 바로 예약을 진행해주세요.");
        }
    }

    private Member getMember(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberException("존재하지 않는 회원입니다."));
    }

    private ReservationTime getTime(Long timeId) {
        return reservationTimeRepository.findById(timeId)
                .orElseThrow(() -> new ReservationTimeException("존재하지 않는 예약 시간입니다."));
    }

    private Theme getTheme(Long themeId) {
        return themeRepository.findById(themeId)
                .orElseThrow(() -> new ThemeException("존재하지 않는 테마입니다."));
    }
}
