package roomescape.service.dto.member;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.Length;
import roomescape.domain.member.Member;

public class MemberCreateRequest {

    @Email(message = "이메일 형식이 맞지 않습니다.")
    @NotBlank(message = "이메일은 반드시 입력되어야 합니다.")
    @Length(max = 30, message = "이메일의 길이는 30글자까지 가능합니다.")
    private final String email;

    @NotBlank(message = "비밀번호는 반드시 입력되어야 합니다.")
    @Length(max = 30, message = "비밀번호 길이는 30글자까지 가능합니다.")
    private final String password;

    @NotBlank(message = "이름은 반드시 입력되어야 합니다.")
    @Length(max = 15, message = "이름 길이는 15글자까지 가능합니다.")
    private final String name;

    public MemberCreateRequest(String email, String password, String name) {
        this.email = email;
        this.password = password;
        this.name = name;
    }

    public Member toMember() {
        return new Member(null, email, password, name);
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getName() {
        return name;
    }
}
