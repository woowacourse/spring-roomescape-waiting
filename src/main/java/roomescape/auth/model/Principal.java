package roomescape.auth.model;

public record Principal(Long memberId, String name, boolean isAdmin) {

}
