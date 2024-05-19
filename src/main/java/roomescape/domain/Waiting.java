package roomescape.domain;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class Waiting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Member member;

    @Embedded
    @AttributeOverride(name = "date", column = @Column(nullable = false))
    private ReservationDate date;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private ReservationTime time;

    @ManyToOne
    @JoinColumn(nullable = false)
    private Theme theme;

    public Waiting() {
    }

    public Waiting(Member member, ReservationDate date, ReservationTime time, Theme theme) {
        this(null, member, date, time, theme);
    }

    public Waiting(Long id, Member member, ReservationDate date, ReservationTime time, Theme theme) {
        this.member = member;
        this.id = id;
        this.date = date;
        this.time = time;
        this.theme = theme;
    }

    public static Waiting createIfFuture(LocalDateTime now, Member member, ReservationDate date, ReservationTime time, Theme theme) {
        LocalDateTime waitingDateTime = LocalDateTime.of(date.getDate(), time.getStartAt());
        if (waitingDateTime.isBefore(now)) {
            throw new IllegalArgumentException(String.format("지나간 시간에 대한 에약 대기는 생성할 수 없습니다. (dateTime: %s)", waitingDateTime));
        }
        return new Waiting(member, date, time, theme);
    }

    public boolean hasSameMemberWith(Reservation reservation) {
        Member reservationMember = reservation.getMember();
        return member.equals(reservationMember);
    }

    public Long getId() {
        return id;
    }

    public Member getMember() {
        return member;
    }

    public ReservationDate getDate() {
        return date;
    }

    public ReservationTime getTime() {
        return time;
    }

    public Theme getTheme() {
        return theme;
    }
}
