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
import java.time.LocalDateTime;
import roomescape.domain.member.Member;
import roomescape.domain.theme.Theme;

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

    @NotNull
    @Column(name = "created_at")
    private LocalDateTime createdAt;

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

    public Reservation(Member Member, LocalDate date, LocalDateTime createdAt, ReservationTime time, Theme theme,
        ReservationStatus status) {
        this(null, Member, date, createdAt, time, theme, status);
    }

    public Reservation(Long id, Member Member, LocalDate date, LocalDateTime createdAt,
        ReservationTime time, Theme theme, ReservationStatus status) {

        this.id = id;
        this.member = Member;
        this.date = date;
        this.createdAt = createdAt;
        this.time = time;
        this.theme = theme;
        this.status = status;
    }

    protected Reservation() {
    }

    public boolean isNotReservedBy(Member member) {
        return !this.member.getId().equals(member.getId());
    }

    public void reserve() {
        this.status = ReservationStatus.RESERVED;
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
