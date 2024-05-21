package roomescape.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import roomescape.domain.reservation.ReservationInfo;
import roomescape.domain.reservation.ReservationDate;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.Theme;
import roomescape.domain.user.Member;
import roomescape.repository.MemberRepository;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;

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

    public ReservationInfo addNewReservation(final String date, final Theme theme, final Member member, final ReservationTime reservationTime) {
        final var newTheme = themeRepository.save(theme);
        final var newMember = memberRepository.save(member);
        final var newTime = timeRepository.save(reservationTime);
        return reservationRepository.save(new ReservationInfo(ReservationDate.from(date), newTime, newTheme, newMember));
    }

    public void addExistReservation(final String date, final Theme theme, final Member member, final ReservationTime time) {
        reservationRepository.save(new ReservationInfo(ReservationDate.from(date), time, theme, member));
    }

}
