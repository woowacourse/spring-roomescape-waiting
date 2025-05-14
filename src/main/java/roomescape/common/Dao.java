package roomescape.common;

import java.util.List;
import java.util.Optional;

public interface Dao<T> {
    T add(T t);

    Optional<T> findById(Long id);

    List<T> findAll();

    void deleteById(Long id);
}
