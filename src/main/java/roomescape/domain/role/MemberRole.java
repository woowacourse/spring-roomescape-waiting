package roomescape.domain.role;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.util.Objects;
import roomescape.domain.member.Member;

@Entity
@Table(name = "role")
public class MemberRole {
    @Id
    private Long id;
    @MapsId
    @OneToOne
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;
    @Enumerated(EnumType.STRING)
    private Role role;

    protected MemberRole() {
    }

    public MemberRole(Member member, Role role) {
        this.member = member;
        this.role = role;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MemberRole other)) {
            return false;
        }
        return Objects.equals(id, other.getMemberId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
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
