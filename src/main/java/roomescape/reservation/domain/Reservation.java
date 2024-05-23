package roomescape.reservation.domain;

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
import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.member.domain.Member;

@Entity
public class Reservation extends AuditedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate date;

    @Enumerated(value = EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "theme_id", nullable = false)
    private Theme theme;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "time_id", nullable = false)
    private ReservationTime reservationTime;

    protected Reservation() {
    }

    public Reservation(Member member, LocalDate date, Theme theme, ReservationTime reservationTime, Status status) {
        validateLastDate(date);
        this.member = member;
        this.date = date;
        this.theme = theme;
        this.reservationTime = reservationTime;
        this.status = status;
    }

    public Reservation(Long id, Member member, LocalDate date, Theme theme, ReservationTime reservationTime, Status status) {
        this.id = id;
        this.member = member;
        this.date = date;
        this.theme = theme;
        this.reservationTime = reservationTime;
        this.status = status;
    }

    private void validateLastDate(LocalDate date) {
        if (date.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("지난 날짜는 예약할 수 없습니다.");
        }
    }

    public boolean sameDate(LocalDate otherDate) {
        return date.equals(otherDate);
    }

    public boolean sameThemeId(Long otherThemeId) {
        return theme.sameThemeId(otherThemeId);
    }

    public boolean sameTimeId(Long otherTimeId) {
        return reservationTime.sameTimeId(otherTimeId);
    }

    public boolean isAfterToday() {
        return date.isAfter(LocalDate.now()) || date.isEqual(LocalDate.now());
    }

    public void updateStatus(Status status) {
        this.status = status;
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

    public String getThemeName() {
        return theme.getName();
    }

    public LocalDate getDate() {
        return date;
    }

    public ReservationTime getTime() {
        return reservationTime;
    }

    public LocalTime getStartAt() {
        return reservationTime.getStartAt();
    }

    public Status getStatus() {
        return status;
    }

    public String getStatusDisplayName() {
        return status.getDisplayName();
    }
}
