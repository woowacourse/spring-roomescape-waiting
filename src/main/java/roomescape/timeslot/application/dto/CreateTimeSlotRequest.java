package roomescape.timeslot.application.dto;

import lombok.AccessLevel;
import lombok.experimental.FieldNameConstants;
import roomescape.common.domain.DomainTerm;
import roomescape.common.validate.Validator;
import roomescape.timeslot.domain.ReservationTime;
import roomescape.timeslot.domain.TimeSlot;

@FieldNameConstants(level = AccessLevel.PRIVATE)
public record CreateTimeSlotRequest(ReservationTime startAt) {

    public CreateTimeSlotRequest {
        validate(startAt);
    }

    public TimeSlot toDomain() {
        return TimeSlot.withoutId(startAt);
    }

    private void validate(final ReservationTime startAt) {
        Validator.of(CreateTimeSlotRequest.class)
                .validateNotNull(Fields.startAt, startAt, DomainTerm.RESERVATION_TIME.label());
    }
}
