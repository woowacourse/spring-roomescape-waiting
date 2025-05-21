package roomescape.waiting.service;

import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.member.domain.Member;
import roomescape.member.service.MemberQueryService;
import roomescape.reservation.domain.ReservationDate;
import roomescape.reservation.domain.ReservationDateTime;
import roomescape.reservation.exception.InAlreadyReservationException;
import roomescape.reservation.service.ReservationQueryService;
import roomescape.reservation.service.command.ReserveCommand;
import roomescape.theme.domain.Theme;
import roomescape.theme.service.ThemeQueryService;
import roomescape.time.service.ReservationTimeQueryService;
import roomescape.waiting.domain.Waiting;
import roomescape.waiting.exception.InAlreadyWaitingException;
import roomescape.waiting.repository.WaitingRepository;

@Service
@RequiredArgsConstructor
public class WaitingManager {

    private final ThemeQueryService themeQueryService;
    private final MemberQueryService memberQueryService;
    private final ReservationTimeQueryService reservationTimeQueryService;
    private final ReservationQueryService reservationQueryService;
    private final WaitingRepository waitingRepository;

    public Waiting findAndDelete(LocalDate date, Long timeId) {
        if (!waitingRepository.existsByDateAndTimeId(date, timeId)) {
            return null;
        }
        Waiting waiting = waitingRepository.findByDateAndTimeId(date, timeId).getFirst();
        waitingRepository.delete(waiting);
        return waiting;
    }

    public Waiting getWaiting(ReserveCommand reserveCommand) {
        Theme theme = themeQueryService.getTheme(reserveCommand.themeId());
        Member member = memberQueryService.getMember(reserveCommand.memberId());
        ReservationDateTime reservationDateTime = ReservationDateTime.create(new ReservationDate(reserveCommand.date()),
                reservationTimeQueryService.getReservationTime(reserveCommand.timeId()));

        validateWaiting(reserveCommand.memberId(), reserveCommand.date(), reserveCommand.timeId());
        return Waiting.builder()
                .reserver(member)
                .reservationDateTime(reservationDateTime)
                .theme(theme)
                .build();
    }

    private void validateWaiting(Long memberId, LocalDate date, Long timeId) {
        if (waitingRepository.existsByMemberIdAndDateAndTimeId(memberId, date, timeId)) {
            throw new InAlreadyWaitingException("이미 예약된 시간입니다.");
        }

        if (reservationQueryService.existsReservation(memberId, date, timeId)) {
            throw new InAlreadyReservationException("해당 유저는 이미 예약했습니다.");
        }
    }
}
