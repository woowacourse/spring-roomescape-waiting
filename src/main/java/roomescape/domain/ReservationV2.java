package roomescape.domain;

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
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "reservation_v2")
@Entity
public class ReservationV2 {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    @Column(nullable = false)
    private LocalDate date;

    @ManyToOne
    @JoinColumn(name = "time_id")
    private ReservationTime time;

    @ManyToOne
    @JoinColumn(name = "theme_id")
    private ReservationTheme theme;

    public ReservationV2(Long id, Member member, LocalDate date, ReservationTime time, ReservationTheme theme) {
        this.id = id;
        this.member = member;
        this.date = date;
        this.time = time;
        this.theme = theme;
    }

    @Builder
    public ReservationV2(Member member, LocalDate date, ReservationTime time, ReservationTheme theme) {
        validateLocalDate(date, time);
        this.member = member;
        this.date = date;
        this.time = time;
        this.theme = theme;
    }

    private void validateLocalDate(final LocalDate date, final ReservationTime time) {
        final LocalDateTime now = LocalDateTime.now();
        final LocalDateTime dateTime = LocalDateTime.of(date, time.getStartAt());
        if (dateTime.isBefore(now) || dateTime.isEqual(now)) {
            throw new IllegalArgumentException("[ERROR] 예약시간은 과거일 수 없습니다.");
        }
    }

}
