package roomescape.reservation.service;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.global.exception.InvalidArgumentException;
import roomescape.global.exception.NotFoundException;
import roomescape.member.domain.Member;
import roomescape.member.service.MemberService;
import roomescape.reservation.controller.response.MyReservationResponse;
import roomescape.reservation.controller.response.ReservationResponse;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationDate;
import roomescape.reservation.domain.ReservationDateTime;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.service.command.ReserveCommand;
import roomescape.theme.domain.Theme;
import roomescape.theme.service.ThemeService;
import roomescape.time.service.ReservationTimeService;

@RequiredArgsConstructor
@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationTimeService reservationTimeService;
    private final ThemeService themeService;
    private final MemberService memberService;

    @Transactional
    public ReservationResponse reserve(ReserveCommand reserveCommand) {
        LocalDate date = reserveCommand.date();
        Long timeId = reserveCommand.timeId();

        isAlreadyReservedTime(date, timeId);

        Reservation reserved = reservationFrom(reserveCommand, date, timeId);
        Reservation saved = reservationRepository.save(reserved);

        return ReservationResponse.from(saved);
    }

    private Reservation reservationFrom(ReserveCommand reserveCommand, LocalDate date, Long timeId) {
        ReservationDateTime reservationDateTime = ReservationDateTime.create(new ReservationDate(date),
                reservationTimeService.getReservationTime(timeId));

        Theme theme = themeService.getTheme(reserveCommand.themeId());
        Member reserver = memberService.getMember(reserveCommand.memberId());

        return Reservation.builder()
                .reservationDateTime(reservationDateTime)
                .reserver(reserver)
                .theme(theme)
                .build();
    }

    private void isAlreadyReservedTime(LocalDate date, Long timeId) {
        if (reservationRepository.existsByDateAndTimeId(date, timeId)) {
            throw new InvalidArgumentException("이미 예약이 존재하는 시간입니다.");
        }
    }

    @Transactional
    public void deleteById(Long id) {
        Reservation reservation = getReservation(id);
        reservationRepository.deleteById(reservation.getId());
    }

    private Reservation getReservation(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("예약을 찾을 수 없습니다."));
    }
}
