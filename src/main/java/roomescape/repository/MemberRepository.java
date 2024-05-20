package roomescape.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import roomescape.model.Member;

public interface MemberRepository extends CrudRepository<Member, Long> {

    Optional<Member> findByEmailAndPassword(String email, String password);

    Optional<Member> findById(Long id);

    List<Member> findAll();
}
