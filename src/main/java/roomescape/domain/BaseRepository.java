package roomescape.domain;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.domain.Specification;
import roomescape.exception.NotFoundException;

public interface BaseRepository<T, ID> {

    T save(T entity);

    Optional<T> findById(ID id);

    T getById(ID id) throws NotFoundException;

    List<T> findAll(Specification<T> specification);

    boolean exists(Specification<T> specification);

    void delete(T entity);

    void deleteByIdOrElseThrow(ID id) throws NotFoundException;
}
