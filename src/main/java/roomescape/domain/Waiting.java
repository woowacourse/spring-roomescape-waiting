package roomescape.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;
import org.hibernate.proxy.HibernateProxy;

@Entity
@Table(name = "waiting")
public class Waiting {
    private static final LocalDate LIMIT_DATE = LocalDate.now();
    private static final LocalTime LIMIT_TIME = LocalTime.now();

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;
    @Column(nullable = false, name = "date")
    private LocalDate date;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "time_id")
    private ReservationTime time;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "theme_id")
    private Theme theme;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "status")
    private ReservationStatus status;

    protected Waiting() {
    }

    public Waiting(Member member, LocalDate date, ReservationTime time, Theme theme) {
        this(null, member, date, time, theme, ReservationStatus.WAITING);
    }

    private Waiting(Long id, Member member, LocalDate date, ReservationTime time, Theme theme,
                    ReservationStatus status) {
        validateDateTime(date, time);
        this.id = id;
        this.member = member;
        this.date = date;
        this.time = time;
        this.theme = theme;
        this.status = status;
    }

    private void validateDateTime(LocalDate date, ReservationTime time) {
        if (date.isBefore(LIMIT_DATE)) {
            throw new IllegalArgumentException("예약 날짜는 예약 가능한 기간보다 이전일 수 없습니다.");
        }

        if (date.isEqual(LIMIT_DATE) && time.isBeforeOrSame(LIMIT_TIME)) {
            throw new IllegalArgumentException("예약 시간은 예약 가능한 시간 이전이거나 같을 수 없습니다.");
        }
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

    public ReservationStatus getStatus() {
        return status;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null) {
            return false;
        }
        Class<?> oEffectiveClass = o instanceof HibernateProxy
                ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass()
                : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy
                ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass()
                : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) {
            return false;
        }
        Waiting waiting = (Waiting) o;
        return getId() != null && Objects.equals(getId(), waiting.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer()
                .getPersistentClass()
                .hashCode() : getClass().hashCode();
    }
}
