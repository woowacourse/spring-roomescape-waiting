package roomescape.domain.reservation;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;
import roomescape.domain.member.Member;

@Entity
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    private Member member;
    private LocalDate date;
    @ManyToOne
    private ReservationTime time;
    @ManyToOne
    private Theme theme;
    private LocalDateTime createdAt;

    public Reservation() {
    }

    public Reservation(Long id, Member member, LocalDate date, ReservationTime time, Theme theme,
                       LocalDateTime createdAt) {
        validateCreatedAtAfterReserveTime(date, time.getStartAt(), createdAt);
        this.id = id;
        this.member = member;
        this.date = date;
        this.time = time;
        this.theme = theme;
        this.createdAt = createdAt;
    }

    public Reservation(Member member, LocalDate date, ReservationTime time, Theme theme, LocalDateTime createdAt) {
        this(null, member, date, time, theme, createdAt);
    }

    private void validateCreatedAtAfterReserveTime(LocalDate date, LocalTime startAt, LocalDateTime createdAt) {
        LocalDateTime reservedDateTime = LocalDateTime.of(date, startAt);
        if (reservedDateTime.isBefore(createdAt)) {
            throw new IllegalArgumentException("현재 시간보다 과거로 예약할 수 없습니다.");
        }
    }

    public Reservation withId(long id) {
        return new Reservation(id, member, date, time, theme, createdAt);
    }

    public boolean isOwnedBy(long memberId) {
        return member.hasId(memberId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Reservation that)) {
            return false;
        }
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
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

    public Member getMember() {
        return member;
    }

    public ReservationTime getTime() {
        return time;
    }

    public Theme getTheme() {
        return theme;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
