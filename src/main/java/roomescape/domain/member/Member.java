package roomescape.domain.member;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;

@Entity
@Getter
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;

    protected Member() {
    }

    private Member(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public static Member of(Long id, String name) {
        return new Member(id, name);
    }

    public static Member createWithoutId(String name) {
        return new Member(null, name);
    }
}
