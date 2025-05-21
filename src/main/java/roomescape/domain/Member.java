package roomescape.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Member {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String name;

    private String sessionId;

    @Enumerated(value = EnumType.STRING)
    private MemberRole role;

    @Builder
    public Member(String email, String password, String name, MemberRole role) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.role = role;
    }


    public Member(final Long id, final String email, final String password, final String name, final String sessionId,
                  final MemberRole role) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.name = name;
        this.sessionId = sessionId;
        this.role = role;
    }

    public void updateSessionId(final String sessionId) {
        this.sessionId = sessionId;
    }
}
