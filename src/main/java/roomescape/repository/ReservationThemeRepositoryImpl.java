package roomescape.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import roomescape.domain.ReservationTheme;
import roomescape.repository.jpa.ReservationThemeJpaRepository;

@Repository
public class ReservationThemeRepositoryImpl implements ReservationThemeRepository {

    private final JdbcTemplate template;
    private final ReservationThemeJpaRepository reservationThemeJpaRepository;

    public ReservationThemeRepositoryImpl(final JdbcTemplate template,
                                          final ReservationThemeJpaRepository reservationThemeJpaRepository) {
        this.template = template;
        this.reservationThemeJpaRepository = reservationThemeJpaRepository;
    }

    @Override
    public Optional<ReservationTheme> findById(final Long id) {
       return reservationThemeJpaRepository.findById(id);
    }

    @Override
    public List<ReservationTheme> findAll() {
        return reservationThemeJpaRepository.findAll();
    }

    @Override
    public List<ReservationTheme> findWeeklyThemeOrderByCountDesc() {
        String sql = """
                SELECT th.id, th.name, th.description, th.thumbnail, COUNT(*) AS reservation_count
                FROM reservation_v2 r
                JOIN reservation_theme th ON r.theme_id = th.id
                WHERE PARSEDATETIME(r.date, 'yyyy-MM-dd') BETWEEN DATEADD('DAY', -7, CURRENT_DATE) AND DATEADD('DAY', -1, CURRENT_DATE)
                GROUP BY th.id, th.name, th.description, th.thumbnail
                ORDER BY reservation_count DESC
                LIMIT 10; 
                """;
        return template.query(sql, reservationThemeRowMapper());
    }

    @Override
    public ReservationTheme save(final ReservationTheme reservationTheme) {
        return reservationThemeJpaRepository.save(reservationTheme);
    }

    @Override
    public int deleteById(final long id) {
        try {
            String sql = "delete from reservation_theme where id = ?";
            return template.update(sql, id);
        } catch (Exception e) {
            throw new IllegalArgumentException("예약 테마를 지울 수 없습니다.");
        }
    }

    @Override
    public boolean existsByName(final String name) {
        return reservationThemeJpaRepository.existsByName(name);
    }

    private RowMapper<ReservationTheme> reservationThemeRowMapper() {
        return (rs, rowNum) -> {
            return new ReservationTheme(
                    rs.getLong("id"),
                    rs.getString("name"),
                    rs.getString("description"),
                    rs.getString("thumbnail")
            );
        };
    }
}
