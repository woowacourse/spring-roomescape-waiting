package roomescape.timeslot.application.dto;

import lombok.AccessLevel;
import lombok.experimental.FieldNameConstants;
import roomescape.common.domain.DomainTerm;
import roomescape.common.validate.Validator;
import roomescape.timeslot.domain.TimeSlot;

import java.time.LocalTime;
import java.util.List;

@FieldNameConstants(level = AccessLevel.PRIVATE)
public record TimeSlotResponse(Long timeId,
                               LocalTime startAt) {

    public TimeSlotResponse {
        validate(timeId, startAt);
    }

    public static TimeSlotResponse from(final TimeSlot domain) {
        return new TimeSlotResponse(
                domain.getId().getValue(),
                domain.getStartAt().getValue());
    }

    public static List<TimeSlotResponse> from(final List<TimeSlot> domains) {
        return domains.stream()
                .map(TimeSlotResponse::from)
                .toList();
    }

    private void validate(final Long id, final LocalTime startAt) {
        Validator.of(TimeSlotResponse.class)
                .validateNotNull(TimeSlotResponse.Fields.timeId, id, DomainTerm.TIME_SLOT_ID.label())
                .validateNotNull(TimeSlotResponse.Fields.startAt, startAt, DomainTerm.RESERVATION_TIME.label());
    }
}
