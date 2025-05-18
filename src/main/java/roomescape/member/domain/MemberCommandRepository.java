package roomescape.member.domain;

public interface MemberCommandRepository {
    Member save(Member member);

    void deleteById(Long id);
}
