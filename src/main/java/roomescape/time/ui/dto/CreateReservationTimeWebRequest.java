package roomescape.time.ui.dto;

import lombok.AccessLevel;
import lombok.experimental.FieldNameConstants;
import roomescape.common.domain.DomainTerm;
import roomescape.common.validate.Validator;
import roomescape.time.application.dto.CreateReservationTimeRequest;
import roomescape.time.domain.TimeValue;

import java.time.LocalTime;

@FieldNameConstants(level = AccessLevel.PRIVATE)
public record CreateReservationTimeWebRequest(LocalTime startAt) {

    public CreateReservationTimeWebRequest {
        validate(startAt);
    }

    public CreateReservationTimeRequest toServiceRequest() {
        return new CreateReservationTimeRequest(TimeValue.from(startAt));
    }

    private void validate(final LocalTime startAt) {
        Validator.of(CreateReservationTimeWebRequest.class)
                .validateNotNull(Fields.startAt, startAt, DomainTerm.RESERVATION_TIME.label());
    }
}
