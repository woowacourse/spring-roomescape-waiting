package roomescape.business.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "reservation",
        uniqueConstraints = @UniqueConstraint(columnNames = {"date", "reservation_time_id", "theme_id"})
)
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate date;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_time_id", nullable = false)
    private ReservationTime time;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "theme_id", nullable = false)
    private Theme theme;

    public Reservation(final Long id, final LocalDate date, final Member member, final ReservationTime time,
                       final Theme theme
    ) {
        validateDate(date);

        this.id = id;
        this.date = date;
        this.member = member;
        this.time = time;
        this.theme = theme;
    }

    private void validateDate(final LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("date 필드가 null 입니다.");
        }
    }

    public Reservation(final LocalDate date, final Member member, final ReservationTime time, final Theme theme) {
        this(null, date, member, time, theme);
    }

    public Reservation() {

    }

    public Long getId() {
        return id;
    }

    public LocalDate getDate() {
        return date;
    }

    public Member getMember() {
        return member;
    }

    public ReservationTime getTime() {
        return time;
    }

    public Theme getTheme() {
        return theme;
    }

    public boolean isSameReservationTime(final ReservationTime targetReservationTime) {
        return time.isSameReservationTime(targetReservationTime);
    }

    public boolean isSameMember(Long id) {
        return member.isSameMember(id);
    }

    public boolean isPast(final LocalDateTime target) {
        return date.isBefore(target.toLocalDate()) ||
                (date.isEqual(target.toLocalDate()) && time.isPast(target.toLocalTime()));
    }
}
