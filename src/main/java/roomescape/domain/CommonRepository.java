package roomescape.domain;

import roomescape.common.exception.NotFoundException;

import java.util.Optional;

public interface CommonRepository<T> {
    Optional<T> findById(Long id);

    T save(T t);

    void deleteById(Long id);

    boolean existsById(Long id);

    default T getById(Long id) {
        return findById(id).orElseThrow(() ->
                new NotFoundException(" (id=" + id + ") 를 찾을 수 없습니다"));
    }
}
