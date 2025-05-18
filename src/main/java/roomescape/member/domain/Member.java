package roomescape.member.domain;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Entity
@Getter
@NoArgsConstructor
@Accessors(fluent = true)
@EqualsAndHashCode(of = "id")
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    private MemberName memberName;

    private String email;

    private String password;

    @Enumerated(EnumType.STRING)
    private MemberRole role;

    public Member(Long id, String name, String email, String password, MemberRole role) {
        this.id = id;
        this.memberName = new MemberName(name);
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public boolean isSameId(long id) {
        return this.id == id;
    }
}
