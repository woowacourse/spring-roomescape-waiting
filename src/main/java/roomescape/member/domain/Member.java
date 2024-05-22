package roomescape.member.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.util.Objects;
import java.util.regex.Pattern;
import roomescape.exception.BadRequestException;

@Entity
public class Member {

    private static final Pattern ILLEGAL_NAME_REGEX = Pattern.compile(".*[^\\w\\s가-힣].*");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String email;
    private String password;
    @Enumerated(EnumType.STRING)
    private MemberRole role;

    protected Member() {
    }

    public Member(String name, String email, String password) {
        this(null, name, email, password, "USER");
    }

    public Member(Long id, String name, String email, String password, String role) {
        validateName(name);
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = MemberRole.valueOf(role);
    }

    public Member(Long id, String name, String email, String password) {
        this(id, name, email, password, "USER");
    }

    private void validateName(String name) {
        if (name.isBlank()) {
            throw new BadRequestException("공백으로 이루어진 이름으로 예약할 수 없습니다.");
        }
        if (ILLEGAL_NAME_REGEX.matcher(name)
                .matches()) {
            throw new BadRequestException("특수문자가 포함된 이름으로 예약을 시도하였습니다.");
        }
    }

    public String getEmail() {
        return email;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPassword() {
        return password;
    }

    public MemberRole getRole() {
        return role;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Member member)) {
            return false;
        }
        return Objects.equals(id, member.id) && Objects.equals(email, member.email) && Objects.equals(password,
                member.password)
                && Objects.equals(name, member.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, email, password, name);
    }

    @Override
    public String toString() {
        return "Member{" + "email='" + email + '\'' + ", id=" + id + ", name='" + name + '\'' + '}';
    }

}
