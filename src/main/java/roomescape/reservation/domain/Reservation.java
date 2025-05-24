package roomescape.reservation.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;
import roomescape.member.domain.Member;
import roomescape.reservationTime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

@Entity
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Member member;

    @Column(nullable = false)
    private LocalDate date;

    @ManyToOne(fetch = FetchType.LAZY)
    private ReservationTime time;

    @ManyToOne(fetch = FetchType.LAZY)
    private Theme theme;

    @Enumerated(value = EnumType.STRING)
    private ReservationStatus status;

    private LocalDateTime createdAt;

    protected Reservation() {
    }

    private Reservation(final Long id, final Member member, final LocalDate date,
                        final ReservationTime time, final Theme theme, final ReservationStatus status,
                        final LocalDateTime createdAt
    ) {
        this.id = id;
        this.member = member;
        this.date = date;
        this.time = time;
        this.theme = theme;
        this.status = status;
        this.createdAt = createdAt;
    }

    public static Reservation createWithoutId(final LocalDateTime now, final Member member,
                                              final LocalDate reservationDate,
                                              final ReservationTime time, final Theme theme,
                                              final ReservationStatus status
    ) {
        validateReservationDateTime(now, reservationDate, time);
        return new Reservation(null, member, reservationDate, time, theme, status, now);
    }

    public static Reservation createWithId(final Long id, final Member member, final LocalDate date,
                                           final ReservationTime time, final Theme theme,
                                           final ReservationStatus status,
                                           final LocalDateTime createdAt
    ) {
        return new Reservation(Objects.requireNonNull(id), member, date, time, theme, status, createdAt);
    }

    private static void validateReservationDateTime(final LocalDateTime now, final LocalDate reservationDate,
                                                    final ReservationTime time) {
        LocalDate nowDate = now.toLocalDate();
        if (reservationDate.isBefore(nowDate)) {
            throw new IllegalArgumentException("예약할 수 없는 날짜와 시간입니다.");
        }

        LocalTime nowTime = now.toLocalTime();
        if (nowDate.isEqual(reservationDate) && time.isBeforeTime(nowTime)) {
            throw new IllegalArgumentException("예약할 수 없는 날짜와 시간입니다.");
        }
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public Reservation assignId(final Long id) {
        return new Reservation(Objects.requireNonNull(id), member, date, time, theme, status, createdAt);
    }

    public boolean isSameTime(final ReservationTime time) {
        return this.time.isSameTime(time);
    }

    public String name() {
        return member.getName();
    }

    public Long timeId() {
        return time.getId();
    }

    public Long memberId() {
        return member.getId();
    }

    public LocalTime reservationTime() {
        return time.getStartAt();
    }

    public Long themeId() {
        return theme.getId();
    }

    public String themeDescription() {
        return theme.getDescription();
    }

    public String themeName() {
        return theme.getName();
    }

    public String themeThumbnail() {
        return theme.getThumbnail();
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

    public Long getId() {
        return id;
    }

    public LocalDate getDate() {
        return date;
    }

    public boolean isWaitingStatus() {
        return status.equals(ReservationStatus.WAITED);
    }

    @Override
    public boolean equals(final Object object) {
        if (!(object instanceof Reservation that)) {
            return false;
        }

        if (getId() == null && that.getId() == null) {
            return false;
        }

        return Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }
}
