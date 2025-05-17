package roomescape.time.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;
import roomescape.common.domain.DomainTerm;
import roomescape.common.validate.Validator;

import java.time.LocalTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@FieldNameConstants(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "reservation_times")
public class ReservationTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = Fields.startAt)
    private LocalTime startAt;

    private ReservationTime(final LocalTime startAt) {
        validate(startAt);
        this.startAt = startAt;
    }

    private ReservationTime(final ReservationTimeId id, final LocalTime startAt) {
        validate(id);
        validate(startAt);
        this.id = id.getValue();
        this.startAt = startAt;
    }

    public static ReservationTime withId(final ReservationTimeId id, final LocalTime startAt) {
        return new ReservationTime(id, startAt);
    }

    public static ReservationTime withoutId(final LocalTime startAt) {
        return new ReservationTime(startAt);
    }

    private static void validate(final LocalTime startAt) {
        Validator.of(ReservationTime.class)
                .validateNotNull(Fields.startAt, startAt, DomainTerm.RESERVATION_TIME.label());
    }

    private static void validate(final ReservationTimeId id) {
        Validator.of(ReservationTime.class)
                .validateNotNull(Fields.id, id, DomainTerm.RESERVATION_TIME_ID.label());
    }

    public boolean isBefore(final LocalTime time) {
        return startAt.isBefore(time);
    }

    public ReservationTimeId getId() {
        return ReservationTimeId.from(id);
    }
}
