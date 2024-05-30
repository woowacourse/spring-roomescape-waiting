package roomescape.model;

import java.util.Objects;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import roomescape.exception.BadRequestException;

@Entity
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    @Enumerated(EnumType.STRING)
    private Role role;
    private String email;
    private String password;

    protected Member() {
    }

    public Member(Long id, String name, Role role, String email, String password) {
        validateNullOrBlank(name, "name");
        this.id = id;
        this.name = name;
        this.role = role;
        this.email = email;
        this.password = password;
    }

    private void validateNullOrBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new BadRequestException(value, fieldName);
        }
    }

    public Long getId() {
        return id;
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

    public Role getRole() {
        return role;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        Member member = (Member) object;
        return Objects.equals(getId(), member.getId()) && Objects.equals(getName(), member.getName())
                && getRole() == member.getRole() && Objects.equals(getEmail(), member.getEmail())
                && Objects.equals(getPassword(), member.getPassword());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getName(), getRole(), getEmail(), getPassword());
    }

    @Override
    public String toString() {
        return "Member{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", role=" + role +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                '}';
    }
}
