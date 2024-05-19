package roomescape.domain.reservation;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import roomescape.domain.member.Member;
import roomescape.domain.theme.Theme;
import roomescape.global.exception.RoomescapeException;

@Table(name = "reservation")
@Entity
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne
    @NotNull
    @JoinColumn(name = "member_id")
    private Member member;

    @NotNull
    @Column(name = "reserved_date")
    private LocalDate date;

    @ManyToOne
    @NotNull
    @JoinColumn(name = "time_id")
    private ReservationTime time;

    @ManyToOne
    @NotNull
    @JoinColumn(name = "theme_id")
    private Theme theme;

    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(name = "status")
    private ReservationStatus status;

    public Reservation(Member Member, String rawDate, ReservationTime time, Theme theme, ReservationStatus status) {
        this(null, Member, rawDate, time, theme, status);
    }

    public Reservation(Long id, Member Member, String rawDate,
        ReservationTime time, Theme theme, ReservationStatus status) {

        validate(rawDate);
        this.id = id;
        this.member = Member;
        this.date = LocalDate.parse(rawDate);
        this.time = time;
        this.theme = theme;
        this.status = status;
    }

    private void validate(String rawDate) {
        try {
            LocalDate.parse(rawDate);
        } catch (DateTimeParseException e) {
            throw new RoomescapeException("잘못된 날짜 형식입니다.");
        }
    }

    protected Reservation() {
    }

    public boolean isNotReservedBy(Member member) {
        return !this.member.getId().equals(member.getId());
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return member.getName();
    }

    public LocalDate getDate() {
        return date;
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

    public ReservationStatus getStatus() {
        return status;
    }
}
