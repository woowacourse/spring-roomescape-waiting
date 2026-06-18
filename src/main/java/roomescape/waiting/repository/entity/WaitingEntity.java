package roomescape.waiting.repository.entity;

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
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import roomescape.reservationtime.repository.entity.ReservationTimeEntity;
import roomescape.theme.repository.entity.ThemeEntity;
import roomescape.waiting.domain.Waiting;

@Entity
@Table(
    name = "waiting",
    uniqueConstraints = @UniqueConstraint(
        name = "unique_reservation_date_time_theme_name",
        columnNames = {"reservation_date", "time_id", "theme_id", "customer_name"}
    )
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class WaitingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String customerName;

    private LocalDate reservationDate;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "time_id")
    private Long timeId;

    @Column(name = "theme_id")
    private Long themeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "time_id", insertable = false, updatable = false)
    private ReservationTimeEntity time;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "theme_id", insertable = false, updatable = false)
    private ThemeEntity theme;

    private WaitingEntity(
        final String customerName,
        final LocalDate reservationDate,
        final Long timeId,
        final Long themeId
    ) {
        this.customerName = customerName;
        this.reservationDate = reservationDate;
        this.timeId = timeId;
        this.themeId = themeId;
    }

    public static WaitingEntity from(final Waiting waiting) {
        return new WaitingEntity(
            waiting.getCustomerNameValue(),
            waiting.getReservationDate(),
            waiting.getTimeId(),
            waiting.getThemeId()
        );
    }

    public Waiting toDomain() {
        return Waiting.of(id, customerName, reservationDate, createdAt, time.toDomain(), theme.toDomain());
    }
}
