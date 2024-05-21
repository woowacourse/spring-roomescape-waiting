package roomescape.service.member.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.List;

public class MemberListResponse {
    private final List<MemberResponse> members;

    @JsonCreator
    public MemberListResponse(List<MemberResponse> members) {
        this.members = members;
    }

    public List<MemberResponse> getMembers() {
        return members;
    }
}
