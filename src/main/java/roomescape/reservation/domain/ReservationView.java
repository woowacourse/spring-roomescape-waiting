package roomescape.reservation.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Subselect;
import org.hibernate.annotations.Synchronize;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.ReservationTime;

@Immutable
@Entity
@Subselect("""
        SELECT CONCAT('R_', r.id) as composite_id,
               r.user_id,
               r.date,
               r.time_id,
               r.theme_id,
               'CONFIRMED' as status,
               0 as waiting_order
        FROM reservations r
        
        UNION ALL
        
        SELECT CONCAT('W_', wr.id) as composite_id,
               wr.user_id,
               wr.date,
               wr.time_id,
               wr.theme_id,
               'WAITING' as status,
               wr.waiting_order
        FROM waiting_reservations wr
        """)
@Synchronize({"reservations", "waiting_reservations"})
@Table(name = "reservation_view")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@FieldNameConstants
public class ReservationView {

    @Id
    @Column
    private String compositeId;

    @Column
    private Long userId;

    @Embedded
    @AttributeOverride(name = ReservationDate.Fields.value, column = @Column(name = Fields.date))
    private ReservationDate date;

    @ManyToOne
    private ReservationTime time;

    @ManyToOne
    private Theme theme;

    @Enumerated(EnumType.STRING)
    @Column(name = Fields.status)
    private ReservationStatus status;

    @Column
    private int waitingOrder;

    public ReservationView(final String compositeId,
                           final Long userId,
                           final ReservationDate date,
                           final ReservationTime time,
                           final Theme theme,
                           final ReservationStatus status,
                           final int waitingOrder) {
        this.compositeId = compositeId;
        this.userId = userId;
        this.date = date;
        this.time = time;
        this.theme = theme;
        this.status = status;
        this.waitingOrder = waitingOrder;
    }

    public Long getId() {
        try {
            validateId();
            return Long.parseLong(compositeId.substring(2));
        } catch (NumberFormatException e) {
            throw new IllegalStateException("Cannot parse ID from compositeId: " + compositeId, e);
        }
    }

    private void validateId() {
        if (compositeId == null || compositeId.length() < 3) {
            throw new IllegalStateException("Invalid compositeId format: " + compositeId);
        }
    }
}
