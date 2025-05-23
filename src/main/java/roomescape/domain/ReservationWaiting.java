package roomescape.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(
        name = "reservation_waiting",
        indexes = {
                @Index(
                        name = "idx_memberId_date_timeId_themeId",
                        columnList = "member_id, date, time_id, theme_id"
                ),
                @Index(
                        name = "idx_date_timeId_themeId",
                        columnList = "date, time_id, theme_id"
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class ReservationWaiting {

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

    @Column(nullable = false)
    @CreatedDate
    private LocalDateTime createdAt;

    public ReservationWaiting(final Member member, final LocalDate date, final ReservationTime time,
                              final ReservationTheme theme) {
        this.member = member;
        this.date = date;
        this.time = time;
        this.theme = theme;
    }
}
