package roomescape.dto.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record WaitingListDeleteCommand(
        @NotNull(message = "WAITING_LIST_ID_NULL")
        Long waitingListId,

        @NotNull(message = "PERSON_NAME_NULL_OR_BLANK")
        @NotBlank(message = "PERSON_NAME_NULL_OR_BLANK")
        String name
) {
}
