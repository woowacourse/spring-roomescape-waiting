package roomescape.member.presentation.dto;

public class SignUpResponse {
    private Long id;

    private SignUpResponse() {
    }

    public SignUpResponse(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }
}
