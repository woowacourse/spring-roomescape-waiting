package roomescape.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import roomescape.domain.reservation.*;
import roomescape.domain.user.Member;

@Component
public class ReservationInserter {
    @Autowired
    ReservationRepository reservationRepository;
    @Autowired
    ReservationTimeRepository timeRepository;
    @Autowired
    ThemeRepository themeRepository;
    @Autowired
    MemberRepository memberRepository;

    public Reservation addNewReservation(final String date,final Theme theme, final Member member, final ReservationTime reservationTime) {
        final var newTheme = themeRepository.save(theme);
        final var newMember = memberRepository.save(member);
        final var newTime = timeRepository.save(reservationTime);
        return reservationRepository.save(new Reservation(null, ReservationDate.from(date), newTime, newTheme, newMember, ReservationStatus.COMPLETE));
    }

    public void addExistReservation(final String date,final Theme theme, final Member member, final ReservationTime time) {
        reservationRepository.save(new Reservation(null, ReservationDate.from(date), time, theme, member,ReservationStatus.COMPLETE));
    }

}
