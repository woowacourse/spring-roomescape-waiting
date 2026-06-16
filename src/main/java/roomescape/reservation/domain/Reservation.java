package roomescape.reservation.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.http.HttpStatus;
import roomescape.exception.business.BusinessException;
import roomescape.member.domain.Member;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

import static jakarta.persistence.GenerationType.IDENTITY;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"date", "time_id", "theme_id"}))
public class Reservation {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "member_id")
    private Member member;

    @Column(nullable = false)
    private LocalDate date;

    @ManyToOne(optional = false)
    @JoinColumn(name = "time_id")
    private ReservationTime time;

    @ManyToOne(optional = false)
    @JoinColumn(name = "theme_id")
    private Theme theme;

    protected Reservation() {
    }

    private Reservation(Long id, Member member, LocalDate date, ReservationTime time, Theme theme) {
        if (member == null) {
            throw new IllegalArgumentException("예약자는 필수입니다.");
        }
        if (date == null) {
            throw new IllegalArgumentException("날짜는 필수입니다.");
        }
        if (time == null) {
            throw new IllegalArgumentException("예약 시간은 필수입니다.");
        }
        if (theme == null) {
            throw new IllegalArgumentException("테마는 필수입니다.");
        }
        this.id = id;
        this.member = member;
        this.date = date;
        this.time = time;
        this.theme = theme;
    }

    public static Reservation of(Member member, LocalDate date, ReservationTime time, Theme theme) {
        Reservation reservation = new Reservation(null, member, date, time, theme);
        if (reservation.isPast()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "이미 지난 시간에는 예약할 수 없습니다.");
        }
        return reservation;
    }

    public static Reservation restore(Long id, Member member, LocalDate date, ReservationTime time, Theme theme) {
        return new Reservation(id, member, date, time, theme);
    }

    public void reschedule(LocalDate newDate, ReservationTime newTime) {
        if (newDate == null) {
            throw new IllegalArgumentException("날짜는 필수입니다.");
        }
        if (newTime == null) {
            throw new IllegalArgumentException("예약 시간은 필수입니다.");
        }
        if (LocalDateTime.of(newDate, newTime.getStartAt()).isBefore(LocalDateTime.now())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "이미 지난 시간으로 변경할 수 없습니다.");
        }
        this.date = newDate;
        this.time = newTime;
    }

    public boolean isPast() {
        return LocalDateTime.of(date, time.getStartAt()).isBefore(LocalDateTime.now());
    }

    public boolean isOwnedBy(Long memberId) {
        return this.member.getId().equals(memberId);
    }

    public Long getId() {
        return id;
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
}