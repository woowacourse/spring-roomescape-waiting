package roomescape.reservation.service;

import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import roomescape.global.exception.InvalidArgumentException;
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

@Component
@RequiredArgsConstructor
public class ReservationManager {

    private final MemberQueryService memberQueryService;
    private final ThemeQueryService themeQueryService;
    private final ReservationTimeQueryService reservationTimeQueryService;
    private final ReservationRepository reservationRepository;

    @Transactional
    public Reservation reserve(ReserveCommand reserveCommand) {
        isAlreadyReservedTime(reserveCommand.date(), reserveCommand.timeId());
        Reservation reserve = reservationFrom(reserveCommand);

        return reservationRepository.save(reserve);
    }

    private Reservation reservationFrom(ReserveCommand reserveCommand) {
        Theme theme = themeQueryService.getTheme(reserveCommand.themeId());
        Member member = memberQueryService.getMember(reserveCommand.memberId());
        ReservationDateTime reservationDateTime = ReservationDateTime.create(new ReservationDate(reserveCommand.date()),
                reservationTimeQueryService.getReservationTime(reserveCommand.timeId()));

        return Reservation.reserve(member, reservationDateTime, theme);
    }

    private void isAlreadyReservedTime(LocalDate date, Long timeId) {
        if (reservationRepository.existsByDateAndTimeId(date, timeId)) {
            throw new InvalidArgumentException("이미 예약이 존재하는 시간입니다.");
        }
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
