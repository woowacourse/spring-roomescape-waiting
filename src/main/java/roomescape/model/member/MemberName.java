package roomescape.model.member;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Embeddable
public class MemberName {

    @NotNull
    @NotBlank
    private String name;

    public MemberName(String name) {
        this.name = name;
    }

    public MemberName() {
    }

    public String getName() {
        return name;
    }
}
