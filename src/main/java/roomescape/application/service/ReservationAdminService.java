package roomescape.application.service;

import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.dto.request.ReservationAdminRegisterDto;
import roomescape.model.Member;
import roomescape.model.Reservation;
import roomescape.model.ReservationTime;
import roomescape.model.Theme;
import roomescape.persistence.MemberRepository;
import roomescape.persistence.ReservationRepository;
import roomescape.persistence.ReservationTimeRepository;
import roomescape.persistence.ThemeRepository;

@Service
@RequiredArgsConstructor
public class ReservationAdminService {

    private final ReservationRepository reservationRepository;
    private final ThemeRepository themeRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final MemberRepository memberRepository;

    public void saveReservation(ReservationAdminRegisterDto registerDto) {
        Member member = memberRepository.findById(registerDto.memberId());
        ReservationTime reservationTime = reservationTimeRepository.findById(registerDto.timeId());
        Theme theme = themeRepository.findById(registerDto.themeId());

        Reservation reservation = new Reservation(registerDto.date(), reservationTime, theme, member, LocalDate.now());

        reservationRepository.save(reservation);
    }
}
