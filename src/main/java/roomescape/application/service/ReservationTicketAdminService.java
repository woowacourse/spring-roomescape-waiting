package roomescape.application.service;

import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.common.exception.DuplicatedException;
import roomescape.dto.request.ReservationAdminRegisterDto;
import roomescape.model.Member;
import roomescape.model.Reservation;
import roomescape.model.ReservationTicket;
import roomescape.model.ReservationTime;
import roomescape.model.Theme;
import roomescape.persistence.repository.MemberRepository;
import roomescape.persistence.repository.ReservationTicketRepository;
import roomescape.persistence.repository.ReservationTimeRepository;
import roomescape.persistence.repository.ThemeRepository;

@Service
@RequiredArgsConstructor
public class ReservationTicketAdminService {

    private final ReservationTicketRepository reservationTicketRepository;
    private final ThemeRepository themeRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final MemberRepository memberRepository;

    public void saveReservation(ReservationAdminRegisterDto registerDto) {
        ReservationTicket reservationTicket = createReservationTicket(registerDto);
        assertReservationIsNotDuplicated(reservationTicket);

        reservationTicketRepository.save(reservationTicket);
    }

    private ReservationTicket createReservationTicket(ReservationAdminRegisterDto registerDto) {
        Member member = memberRepository.findById(registerDto.memberId());
        ReservationTime reservationTime = reservationTimeRepository.findById(registerDto.timeId());
        Theme theme = themeRepository.findById(registerDto.themeId());

        return new ReservationTicket(
                new Reservation(registerDto.date(), reservationTime, theme, member, LocalDate.now()));
    }

    private void assertReservationIsNotDuplicated(ReservationTicket reservationTicket) {
        if (reservationTicketRepository.isDuplicated(reservationTicket.getReservation())) {
            throw new DuplicatedException("이미 예약이 존재합니다.");
        }
    }
}
