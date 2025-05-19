package roomescape.application.reservation.command;

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

    public CreateWaitingService(WaitingRepository waitingRepository,
                                ReservationRepository reservationRepository,
                                MemberRepository memberRepository) {
        this.waitingRepository = waitingRepository;
        this.reservationRepository = reservationRepository;
        this.memberRepository = memberRepository;
    }

    public Long create(CreateWaitingCommand createParameter) {
        Reservation reservation = getReservation(createParameter);
        Member member = getMember(createParameter);
        Waiting waiting = new Waiting(
                member,
                reservation.getDate(),
                reservation.getTime(),
                reservation.getTheme()
        );
        return waitingRepository.save(waiting)
                .getId();
    }

    private Reservation getReservation(CreateWaitingCommand createParameter) {
        return reservationRepository.findByDateAndTimeIdAndThemeId(
                        createParameter.reservationDate(),
                        createParameter.timeId(),
                        createParameter.themeId()
                )
                .orElseThrow(() -> new WaitingException("예약이 존재하지 않아 대기를 신청할 수 없습니다. 바로 예약을 진행해주세요."));
    }

    private Member getMember(CreateWaitingCommand createParameter) {
        return memberRepository.findById(createParameter.memberId())
                .orElseThrow(() -> new MemberException("존재하지 않는 회원입니다."));
    }
}
