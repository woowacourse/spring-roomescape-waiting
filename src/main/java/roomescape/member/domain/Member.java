package roomescape.member.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NonNull
    @Column(nullable = false)
    private String name;

    @NonNull
    @Column(nullable = false)
    private String email;

    @NonNull
    @Column(nullable = false)
    private String password;

    @NonNull
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private MemberRole role;

    public Member(final Long id, @NonNull final String name, @NonNull final String email,
                  @NonNull final String password,
                  @NonNull final MemberRole role) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public Member(final Long id, @NonNull final String name, @NonNull final String email,
                  @NonNull final String password,
                  @NonNull final String role) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        try {
            this.role = MemberRole.valueOf(role);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("유효하지 않은 회원 역할입니다.: " + role);
        }
    }

    public Member(@NonNull final String name, @NonNull final String email, @NonNull final String password) {
        this.id = null;
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = MemberRole.MEMBER;
    }

    public Member(@NonNull Long id, @NonNull final String name, @NonNull final String email,
                  @NonNull final MemberRole role) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = null;
        this.role = role;
    }

    public boolean matchesPassword(final String password) {
        return this.password.equals(password);
    }

    public boolean isAdmin() {
        return this.role.isAdmin();
    }
}
