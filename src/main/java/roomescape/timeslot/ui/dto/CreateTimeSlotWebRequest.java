package roomescape.timeslot.ui.dto;

import lombok.AccessLevel;
import lombok.experimental.FieldNameConstants;
import roomescape.common.domain.DomainTerm;
import roomescape.common.validate.Validator;
import roomescape.timeslot.application.dto.CreateTimeSlotRequest;
import roomescape.timeslot.domain.ReservationTime;

import java.time.LocalTime;

@FieldNameConstants(level = AccessLevel.PRIVATE)
public record CreateTimeSlotWebRequest(LocalTime startAt) {

    public CreateTimeSlotWebRequest {
        validate(startAt);
    }

    public CreateTimeSlotRequest toServiceRequest() {
        return new CreateTimeSlotRequest(ReservationTime.from(startAt));
    }

    private void validate(final LocalTime startAt) {
        Validator.of(CreateTimeSlotWebRequest.class)
                .validateNotNull(Fields.startAt, startAt, DomainTerm.RESERVATION_TIME.label());
    }
}
