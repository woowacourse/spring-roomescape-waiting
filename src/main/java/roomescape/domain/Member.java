package roomescape.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.util.Objects;

@Entity
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    protected Member() {
    }

    public Member(Long id, String name) {
        Objects.requireNonNull(name, "이름은 필수값입니다.");
        this.id = id;
        this.name = name;
    }

    public static Member createWithoutId(String name) {
        return new Member(null, name);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        Member member = (Member) object;
        if (id != null && member.id != null) {
            return Objects.equals(id, member.id);
        }
        return Objects.equals(name, member.name);
    }

    @Override
    public int hashCode() {
        if (id != null) {
            return Objects.hash(id);
        }
        return Objects.hash(name);
    }
}
