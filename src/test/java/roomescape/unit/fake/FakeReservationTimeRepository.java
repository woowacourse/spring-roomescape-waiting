package roomescape.unit.fake;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.domain.time.AvailableReservationTime;
import roomescape.domain.time.ReservationTime;
import roomescape.repository.ReservationTimeRepository;

public class FakeReservationTimeRepository implements ReservationTimeRepository {
    private final List<ReservationTime> values = new ArrayList<>();
    private final AtomicLong increment = new AtomicLong(1);

    @Override
    public ReservationTime save(final LocalTime time) {
        ReservationTime rt = new ReservationTime(increment.getAndIncrement(), time);
        values.add(rt);
        return rt;
    }

    @Override
    public List<ReservationTime> findAll() {
        return Collections.unmodifiableList(values);
    }

    @Override
    public void deleteById(final Long id) {
        values.removeIf(time -> time.getId().equals(id));
    }

    @Override
    public Optional<ReservationTime> findById(final Long id) {
        return values.stream().filter(time -> time.getId().equals(id)).findFirst();
    }

    @Override
    public boolean existByStartAt(final LocalTime startAt) {
        return values.stream().anyMatch(time -> time.getStartAt().equals(startAt));
    }

    @Override
    public List<AvailableReservationTime> findAllAvailableReservationTimes(final LocalDate date, final Long themeId) {
        return values.stream()
                .map(time -> new AvailableReservationTime(time, false))
                .toList();
    }
}
