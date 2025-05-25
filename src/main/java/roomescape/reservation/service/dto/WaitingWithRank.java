package roomescape.reservation.service.dto;

import lombok.AccessLevel;
import lombok.experimental.FieldNameConstants;
import roomescape.common.utils.Validator;
import roomescape.reservation.domain.Waiting;

@FieldNameConstants(level = AccessLevel.PRIVATE)
public record WaitingWithRank(
        Waiting waiting,
        Integer rank
) {

    public WaitingWithRank {
        validate(waiting, rank);
    }

    private void validate(final Waiting waiting, final Integer rank) {
        Validator.of(WaitingWithRank.class)
                .notNullField(Fields.waiting, waiting)
                .notNullField(Fields.rank, rank);
    }
}
