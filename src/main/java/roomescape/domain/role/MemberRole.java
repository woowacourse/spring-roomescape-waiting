package roomescape.domain.role;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import roomescape.domain.member.Member;

@Entity
public class MemberRole {

    @Id
    private long memberId;
    private String name;
    private Role role;

    MemberRole() {
    }

    public MemberRole(long memberId, String name, Role role) {
        this.memberId = memberId;
        this.name = name;
        this.role = role;
    }

    public MemberRole(long memberId, String name) {
        this(memberId, name, Role.MEMBER);
    }

    public MemberRole(Member member, Role role) {
        this(member.getId(), member.getName(), role);
    }

    public boolean isAdmin() {
        return role.isAdmin();
    }

    public boolean hasRoleOf(Role other) {
        return role == other;
    }

    public long getMemberId() {
        return memberId;
    }

    public Role getRole() {
        return role;
    }

    public String getMemberName() {
        return name;
    }

    public String getRoleName() {
        return role.name();
    }
}
