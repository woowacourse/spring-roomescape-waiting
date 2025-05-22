package roomescape.reservation.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.member.domain.Member;
import roomescape.member.service.MemberQueryService;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationDate;
import roomescape.reservation.domain.ReservationDateTime;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.service.command.ReserveCommand;
import roomescape.theme.domain.Theme;
import roomescape.theme.service.ThemeQueryService;
import roomescape.time.service.ReservationTimeQueryService;

@Service
@RequiredArgsConstructor
public class ReservationManager {

    private final MemberQueryService memberQueryService;
    private final ThemeQueryService themeQueryService;
    private final ReservationTimeQueryService reservationTimeQueryService;
    private final ReservationRepository reservationRepository;

    @Transactional
    public Reservation reserve(ReserveCommand reserveCommand) {
        Theme theme = themeQueryService.getTheme(reserveCommand.themeId());
        Member member = memberQueryService.getMember(reserveCommand.memberId());
        ReservationDateTime reservationDateTime = ReservationDateTime.create(new ReservationDate(reserveCommand.date()),
                reservationTimeQueryService.getReservationTime(reserveCommand.timeId()));

        Reservation reserve = Reservation.reserve(member, reservationDateTime, theme);

        return reservationRepository.save(reserve);
    }

    @Transactional
    public Reservation waiting(ReserveCommand reserveCommand) {
        Theme theme = themeQueryService.getTheme(reserveCommand.themeId());
        Member member = memberQueryService.getMember(reserveCommand.memberId());
        ReservationDateTime reservationDateTime = ReservationDateTime.create(new ReservationDate(reserveCommand.date()),
                reservationTimeQueryService.getReservationTime(reserveCommand.timeId()));

        Reservation waiting = Reservation.waiting(member, reservationDateTime, theme);

        return reservationRepository.save(waiting);
    }

    public void delete(Reservation reservation) {
        reservationRepository.delete(reservation);
    }
}
