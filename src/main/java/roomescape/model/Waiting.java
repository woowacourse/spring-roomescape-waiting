package roomescape.model;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import roomescape.model.member.Member;
import roomescape.model.theme.Theme;
import roomescape.service.dto.ReservationDto;

@Entity
public class Waiting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @NotNull
    private LocalDate date;
    @NotNull
    private LocalDateTime created_at;

    @NotNull
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "time_id")
    private ReservationTime time;
    @NotNull
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "theme_id")
    private Theme theme;
    @NotNull
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "member_id")
    private Member member;

    private Waiting(long id, LocalDate date, LocalDateTime created_at, ReservationTime time, Theme theme,
                    Member member) {
        this.id = id;
        this.date = date;
        this.created_at = created_at;
        this.time = time;
        this.theme = theme;
        this.member = member;
    }

    public Waiting(ReservationDto reservationDto) {
        this(0,
                reservationDto.getDate(),
                LocalDateTime.now(),
                new ReservationTime(reservationDto.getTimeId(), null),
                new Theme(reservationDto.getThemeId(), null, null, null),
                new Member(reservationDto.getMemberId(), null, null, null, null));
    }

    public Waiting() {
    }

    public long getId() {
        return id;
    }

    public LocalDate getDate() {
        return date;
    }

    public LocalDateTime getCreated_at() {
        return created_at;
    }

    public ReservationTime getTime() {
        return time;
    }

    public Theme getTheme() {
        return theme;
    }

    public Member getMember() {
        return member;
    }
}
