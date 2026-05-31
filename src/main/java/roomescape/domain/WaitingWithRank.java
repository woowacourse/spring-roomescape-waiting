package roomescape.domain;

public record WaitingWithRank(Long id, Member owner, Slot slot, int rank) {
}
