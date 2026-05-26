package roomescape.dao;

import java.util.List;
import java.util.Optional;

public interface CommonDao<T> {

    List<T> findAll();

    Optional<T> findById(Long id);

    T insert(T t);

    T update(T t);

    int delete(Long id);

    boolean existsById(Long id);
}
