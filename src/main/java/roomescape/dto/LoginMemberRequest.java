package roomescape.dto;

import roomescape.domain.Name;
import roomescape.domain.Role;

public record LoginMemberRequest(long id, Name name, Role role) {

}
