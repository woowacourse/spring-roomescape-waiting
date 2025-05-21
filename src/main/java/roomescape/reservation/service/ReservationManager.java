package roomescape.reservation.service;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.member.domain.Member;
import roomescape.member.service.MemberQueryService;
import roomescape.reservation.controller.response.MyReservationResponse;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationDate;
import roomescape.reservation.domain.ReservationDateTime;
import roomescape.reservation.service.command.ReserveCommand;
import roomescape.theme.domain.Theme;
import roomescape.theme.service.ThemeQueryService;
import roomescape.time.service.ReservationTimeQueryService;
import roomescape.waiting.service.WaitingQueryService;

@Service
@RequiredArgsConstructor
public class ReservationManager {

    private final MemberQueryService memberQueryService;
    private final ThemeQueryService themeQueryService;
    private final ReservationTimeQueryService reservationTimeQueryService;
    private final ReservationQueryService reservationQueryService;

    public Reservation getReservation(ReserveCommand reserveCommand) {
        Theme theme = themeQueryService.getTheme(reserveCommand.themeId());
        Member member = memberQueryService.getMember(reserveCommand.memberId());
        ReservationDateTime reservationDateTime = ReservationDateTime.create(new ReservationDate(reserveCommand.date()),
                reservationTimeQueryService.getReservationTime(reserveCommand.timeId()));

        return Reservation.builder()
                .theme(theme)
                .reserver(member)
                .reservationDateTime(reservationDateTime)
                .build();
    }
}
