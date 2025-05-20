package roomescape.repository.member;

import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.Repository;
import roomescape.domain.member.Reserver;

@org.springframework.stereotype.Repository
public interface MemberRepository extends Repository<Reserver, Long> {

    Reserver save(Reserver reserver);

    Optional<Reserver> findByUsername(String username);

    boolean existsByUsername(String username);

    List<Reserver> findAll();

    Optional<Reserver> findById(long id);
}
