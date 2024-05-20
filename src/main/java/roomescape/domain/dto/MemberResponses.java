package roomescape.domain.dto;

import java.util.List;

public class MemberResponses {
    private final List<MemberResponse> data;

    public MemberResponses(final List<MemberResponse> data) {
        this.data = data;
    }

    public List<MemberResponse> getData() {
        return data;
    }
}
