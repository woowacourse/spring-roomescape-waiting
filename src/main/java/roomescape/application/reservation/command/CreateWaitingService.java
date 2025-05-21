package roomescape.application.reservation.command;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.application.reservation.command.dto.CreateWaitingCommand;
import roomescape.domain.member.Member;
import roomescape.domain.member.repository.MemberRepository;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.Waiting;
import roomescape.domain.reservation.repository.ReservationRepository;
import roomescape.domain.reservation.repository.WaitingRepository;
import roomescape.infrastructure.error.exception.MemberException;
import roomescape.infrastructure.error.exception.WaitingException;

@Service
@Transactional
public class CreateWaitingService {

    private final WaitingRepository waitingRepository;
    private final ReservationRepository reservationRepository;
    private final MemberRepository memberRepository;
    private final Clock clock;

    public CreateWaitingService(WaitingRepository waitingRepository,
                                ReservationRepository reservationRepository,
                                MemberRepository memberRepository,
                                Clock clock) {
        this.waitingRepository = waitingRepository;
        this.reservationRepository = reservationRepository;
        this.memberRepository = memberRepository;
        this.clock = clock;
    }

    public Long request(CreateWaitingCommand createParameter) {
        validateNotAlreadyReservedOrWaiting(createParameter);
        Reservation reservation = getExistingReservation(createParameter);
        Member member = getMember(createParameter.memberId());
        Waiting waiting = createWaiting(member, reservation);
        return waitingRepository.save(waiting).getId();
    }

    private void validateNotAlreadyReservedOrWaiting(CreateWaitingCommand createParameter) {
        if (isAlreadyReservedOrWaiting(createParameter)) {
            throw new WaitingException("이미 예약했거나 대기 중입니다.");
        }
    }

    private boolean isAlreadyReservedOrWaiting(CreateWaitingCommand createParameter) {
        boolean isAlreadyReserved = reservationRepository.existsByDateAndTimeIdAndThemeIdAndMemberId(
                createParameter.reservationDate(),
                createParameter.timeId(),
                createParameter.themeId(),
                createParameter.memberId()
        );
        boolean isAlreadyWaiting = waitingRepository.existsByDateAndTimeIdAndThemeIdAndMemberId(
                createParameter.reservationDate(),
                createParameter.timeId(),
                createParameter.themeId(),
                createParameter.memberId()
        );
        return isAlreadyReserved || isAlreadyWaiting;
    }

    private Reservation getExistingReservation(CreateWaitingCommand createParameter) {
        LocalDate date = createParameter.reservationDate();
        Long timeId = createParameter.timeId();
        Long themeId = createParameter.themeId();
        return reservationRepository.findByDateAndTimeIdAndThemeId(date, timeId, themeId)
                .orElseThrow(() -> new WaitingException("예약이 존재하지 않아 대기를 신청할 수 없습니다. 바로 예약을 진행해주세요."));
    }

    private Member getMember(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberException("존재하지 않는 회원입니다."));
    }

    private Waiting createWaiting(Member member, Reservation reservation) {
        Waiting waiting = new Waiting(member, reservation.getDate(), reservation.getTime(), reservation.getTheme());
        waiting.validateWaitable(LocalDateTime.now(clock));
        return waiting;
    }
}
