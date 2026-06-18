package roomescape.repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.FluentQuery;

import java.util.function.Function;

@SuppressWarnings("all")
abstract class AbstractFakeRepository<T, ID extends Number> implements JpaRepository<T, ID> {

    protected final Map<Long, T> store = new HashMap<>();
    protected final AtomicLong sequence = new AtomicLong(1);

    protected abstract Long getId(T entity);

    protected abstract T withId(T entity, Long id);

    @Override
    public <S extends T> S save(S entity) {
        Long id = getId((T) entity);
        if (id == null) {
            id = sequence.getAndIncrement();
            entity = (S) withId(entity, id);
        }
        store.put(id, entity);
        return entity;
    }

    @Override
    public <S extends T> List<S> saveAll(Iterable<S> entities) {
        List<S> result = new ArrayList<>();
        entities.forEach(e -> result.add(save(e)));
        return result;
    }

    @Override
    public Optional<T> findById(ID id) {
        return Optional.ofNullable(store.get(((Number) id).longValue()));
    }

    @Override
    public boolean existsById(ID id) {
        return store.containsKey(((Number) id).longValue());
    }

    @Override
    public List<T> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public List<T> findAllById(Iterable<ID> ids) {
        List<T> result = new ArrayList<>();
        ids.forEach(id -> findById(id).ifPresent(result::add));
        return result;
    }

    @Override
    public long count() {
        return store.size();
    }

    @Override
    public void deleteById(ID id) {
        store.remove(((Number) id).longValue());
    }

    @Override
    public void delete(T entity) {
        deleteById((ID) getId(entity));
    }

    @Override
    public void deleteAllById(Iterable<? extends ID> ids) {
        ids.forEach(this::deleteById);
    }

    @Override
    public void deleteAll(Iterable<? extends T> entities) {
        entities.forEach(this::delete);
    }

    @Override
    public void deleteAll() {
        store.clear();
    }

    @Override public <S extends T> S saveAndFlush(S entity) { return save(entity); }
    @Override public <S extends T> List<S> saveAllAndFlush(Iterable<S> entities) { return saveAll(entities); }
    @Override public void deleteAllInBatch(Iterable<T> entities) { deleteAll(entities); }
    @Override public void deleteAllByIdInBatch(Iterable<ID> ids) { deleteAllById(ids); }
    @Override public void deleteAllInBatch() { deleteAll(); }
    @Override public T getOne(ID id) { return findById(id).orElse(null); }
    @Override public T getById(ID id) { return findById(id).orElse(null); }
    @Override public T getReferenceById(ID id) { return findById(id).orElse(null); }
    @Override public void flush() {}
    @Override public List<T> findAll(Sort sort) { return findAll(); }
    @Override public Page<T> findAll(Pageable pageable) { throw new UnsupportedOperationException(); }
    @Override public <S extends T> Optional<S> findOne(Example<S> example) { throw new UnsupportedOperationException(); }
    @Override public <S extends T> List<S> findAll(Example<S> example) { throw new UnsupportedOperationException(); }
    @Override public <S extends T> List<S> findAll(Example<S> example, Sort sort) { throw new UnsupportedOperationException(); }
    @Override public <S extends T> Page<S> findAll(Example<S> example, Pageable pageable) { throw new UnsupportedOperationException(); }
    @Override public <S extends T> long count(Example<S> example) { throw new UnsupportedOperationException(); }
    @Override public <S extends T> boolean exists(Example<S> example) { throw new UnsupportedOperationException(); }
    @Override public <S extends T, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) { throw new UnsupportedOperationException(); }
}
