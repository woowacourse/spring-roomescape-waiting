package roomescape.domain;

import jakarta.persistence.*;
import roomescape.exception.ReservationException;

import java.util.Objects;

@Entity
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;

    @Enumerated(EnumType.STRING)
    private MemberRole role;
    private String email;
    private String password;

    protected Member() {
    }

    private Member(final Long id, final String name, final MemberRole role, final String email, final String password) {
        if (name.length() < 2 || name.length() > 10) {
            throw new ReservationException("예약자명은 2글자에서 10글자까지만 가능합니다.");
        }
        this.id = id;
        this.name = name;
        this.role = role;
        this.email = email;
        this.password = password;
    }

    public static Member createNew(String name, MemberRole role, String email, String password) {
        return new Member(null, name, role, email, password);
    }

    public Long getId() {
        return id;
    }

    public MemberRole getRole() {
        return role;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Member member = (Member) o;
        return Objects.equals(id, member.id) && Objects.equals(name, member.name) && Objects.equals(role, member.role) && Objects.equals(email, member.email) && Objects.equals(password, member.password);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, role, email, password);
    }
}
