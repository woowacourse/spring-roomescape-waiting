package roomescape.service;

import org.springframework.stereotype.Component;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberRepository;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationDate;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.ReservationTimeRepository;
import roomescape.domain.reservation.Slot;
import roomescape.domain.reservation.SlotRepository;
import roomescape.domain.theme.Theme;
import roomescape.domain.theme.ThemeRepository;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
public class ReservationAssembler {

    private final Clock clock;
    private final MemberRepository members;
    private final ReservationTimeRepository reservationTimes;
    private final ThemeRepository themes;
    private final SlotRepository slots;

    public ReservationAssembler(
            Clock clock,
            MemberRepository members,
            ReservationTimeRepository reservationTimes,
            ThemeRepository themes,
            SlotRepository slots
    ) {
        this.clock = clock;
        this.members = members;
        this.reservationTimes = reservationTimes;
        this.themes = themes;
        this.slots = slots;
    }

    public Reservation from(ReservationCreateCommand command) {
        return of(command.getMemberId(), command.getDate(), command.getTimeId(), command.getThemeId());
    }

    public Reservation from(ReservationUpdateCommand command) {
        return of(command.getMemberId(), command.getDate(), command.getTimeId(), command.getThemeId());
    }

    private Reservation of(Long memberId, LocalDate date, Long timeId, Long themeId) {
        LocalDateTime now = LocalDateTime.now(clock);
        Member member = members.getById(memberId);
        ReservationTime time = reservationTimes.getById(timeId);
        Theme theme = themes.getById(themeId);
        Slot slot = slots.findOrCreate(new ReservationDate(date), time, theme, now);
        return Reservation.create(member, slot);
    }
}
