package roomescape.model.member;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Embeddable
public class Password {

    @NotNull
    @NotBlank
    private String password;

    public Password(String password) {
        this.password = password;
    }

    public Password() {
    }

    public String getPassword() {
        return password;
    }
}
