package roomescape.reservationwaiting.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Builder;
import roomescape.member.domain.Member;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

public class ReservationWaiting {

    private final Long id;
    private final Member member;
    private final LocalDate date;
    private final ReservationTime time;
    private final Theme theme;

    @Builder(access = lombok.AccessLevel.PRIVATE)
    private ReservationWaiting(Long id, Member member, LocalDate date, ReservationTime time, Theme theme) {
        this.id = id;
        this.member = member;
        this.date = date;
        this.time = time;
        this.theme = theme;
    }

    public static ReservationWaiting restore(Long id, Member member, LocalDate date, ReservationTime time,
                                             Theme theme) {
        return ReservationWaiting.builder()
                .id(id)
                .member(member)
                .date(date)
                .time(time)
                .theme(theme)
                .build();
    }

    public boolean isPast() {
        return LocalDateTime.of(date, time.getStartAt()).isBefore(LocalDateTime.now());
    }

    public boolean isOwnedBy(Long memberId) {
        return this.member.getId().equals(memberId);
    }

    public Long getId() { return id; }
    public Member getMember() { return member; }
    public LocalDate getDate() { return date; }
    public ReservationTime getTime() { return time; }
    public Theme getTheme() { return theme; }
}