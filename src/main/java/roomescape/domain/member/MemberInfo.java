package roomescape.domain.member;

public class MemberInfo {

    private final long id;
    private final String name;

    public MemberInfo(long id, String name) {
        this.id = id;
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
