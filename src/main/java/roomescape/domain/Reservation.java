package roomescape.domain;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "reservation")
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Member member;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @ManyToOne(fetch = FetchType.LAZY)
    private ReservationTime time;

    @ManyToOne(fetch = FetchType.LAZY)
    private Theme theme;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ReservationStatus status;

    protected Reservation() {
    }

    private Reservation(final Member member, final LocalDate date, final ReservationTime time, final Theme theme, ReservationStatus status) {
        this(null, member, date, time, theme, status);
    }

    public Reservation(final Long id, final Member member, final LocalDate date, final ReservationTime time,
                       final Theme theme, final ReservationStatus status) {
        this.id = id;
        this.member = member;
        this.date = date;
        this.time = time;
        this.theme = theme;
        this.status = status;
    }

    public static Reservation reserved(final Member member, final LocalDate date, final ReservationTime time, final Theme theme) {
        return new Reservation(member, date, time, theme, ReservationStatus.RESERVED);
    }

    public static Reservation waiting(final Member member, final LocalDate date, final ReservationTime time, final Theme theme) {
        return new Reservation(member, date, time, theme, ReservationStatus.WAITING);
    }

    public void changeToReserved() {
        this.status = ReservationStatus.RESERVED;
    }

    public boolean isWaiting() {
        return this.status.isWaiting();
    }

    public Member getMember() {
        return member;
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

    public Long getId() {
        return id;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public Long getTimeId() {
        return time.getId();
    }

    public Long getThemeId() {
        return theme.getId();
    }

    public Long getMemberId() {
        return member.getId();
    }
}
