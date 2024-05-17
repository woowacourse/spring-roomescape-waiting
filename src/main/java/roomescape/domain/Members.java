package roomescape.domain;

import java.util.Collections;
import java.util.List;

public class Members {
    private final List<Member> members;

    public Members(List<Member> members) {
        this.members = members;
    }

    public List<Member> getMembers() {
        return Collections.unmodifiableList(members);
    }
}
