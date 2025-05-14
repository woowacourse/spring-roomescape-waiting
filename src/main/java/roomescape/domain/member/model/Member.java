package roomescape.domain.member.model;

import lombok.Builder;
import lombok.Getter;

@Getter
public class Member {

    private Long id;

    private final String name;

    private final String email;

    private final String password;

    //TODO : Member 역할을 Enum 객체로 표시해도 괜찮을까? 상속?
    private final Role role;

    @Builder
    public Member(String password, Role role, String email, String name, Long id) {
        this.password = password;
        this.role = role;
        this.email = email;
        this.name = name;
        this.id = id;
    }
}
