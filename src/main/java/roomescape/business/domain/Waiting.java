package roomescape.business.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "waiting")
public class Waiting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne
    @JoinColumn(name = "theme_id")
    private Theme theme;

    @ManyToOne
    @JoinColumn(name = "reservation_time_id")
    private ReservationTime time;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public Waiting() {

    }

    public Waiting(final Member member, final Theme theme, final ReservationTime time,
                   final LocalDate date) {
        validateDate(date);
        this.member = member;
        this.theme = theme;
        this.time = time;
        this.date = date;
        this.createdAt = LocalDateTime.now();
    }

    private void validateDate(final LocalDate date) {
        if(date == null) {
            throw new IllegalArgumentException("date 필드가 null 입니다.");
        }
    }

    public Long getId() {
        return id;
    }

    public Member getMember() {
        return member;
    }

    public Theme getTheme() {
        return theme;
    }

    public ReservationTime getTime() {
        return time;
    }

    public LocalDate getDate() {
        return date;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public boolean isPast(LocalDate today) {
        return date.isBefore(today) || (date.isEqual(today) && time.isPast());
    }
}
