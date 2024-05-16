package roomescape.domain.role;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import roomescape.domain.member.Member;

@Entity
public class MemberRole {
    @Id
    private Long id;
    @MapsId
    @OneToOne(optional = false)
    private Member member;
    @Enumerated(EnumType.STRING)
    private Role role;

    MemberRole() {
    }

    public MemberRole(Member member, Role role) {
        this.member = member;
        this.role = role;
    }

    public long getMemberId() {
        return member.getId();
    }

    public String getMemberName() {
        return member.getName();
    }

    public Role getRole() {
        return role;
    }
}
