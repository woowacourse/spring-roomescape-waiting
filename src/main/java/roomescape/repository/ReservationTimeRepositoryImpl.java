package roomescape.repository;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import roomescape.domain.ReservationTime;
import roomescape.repository.jpa.ReservationTimeJpaRepository;

@Repository
public class ReservationTimeRepositoryImpl implements ReservationTimeRepository {

    private final ReservationTimeJpaRepository reservationTimeJpaRepository;
    private final JdbcTemplate template;

    public ReservationTimeRepositoryImpl(final ReservationTimeJpaRepository reservationTimeJpaRepository, final JdbcTemplate template) {
        this.reservationTimeJpaRepository = reservationTimeJpaRepository;
        this.template = template;
    }

    @Override
    public Optional<ReservationTime> findById(final Long id) {
        return reservationTimeJpaRepository.findById(id);
    }

    @Override
    public List<ReservationTime> findAll() {
        return reservationTimeJpaRepository.findAll();
    }

    @Override
    public ReservationTime save(final ReservationTime reservationTime) {
        return reservationTimeJpaRepository.save(reservationTime);
    }

    @Override
    public int deleteById(final long id) {
        try {
            String sql = "delete from reservation_time where id = ?";
            return template.update(sql, id);
        } catch (Exception e) {
            throw new IllegalArgumentException("예약 시간을 지울 수 없습니다.");
        }
    }

    @Override
    public boolean existsByStartAt(final LocalTime startAt) {
        return reservationTimeJpaRepository.existsByStartAt(startAt);
    }
}
