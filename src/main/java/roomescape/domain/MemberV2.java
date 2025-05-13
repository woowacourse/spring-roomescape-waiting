package roomescape.domain;

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
public class MemberV2 {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    private String email;

    private String password;

    private String name;

    private String sessionId;

    @Enumerated(value = EnumType.STRING)
    private MemberRole role;

    @Builder
    public MemberV2(String email, String password, String name, String sessionId, MemberRole role) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.sessionId = sessionId;
        this.role = role;
    }
}
