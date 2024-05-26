package roomescape.infrastructure.persistence;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;
import roomescape.domain.member.Member;
import roomescape.domain.reservation.Reservation;
import roomescape.repository.ReservationRepository;

@Repository
class ReservationPersistenceAdapter implements ReservationRepository {

    private final SpringDataJpaReservationRepository repository;
    private final EntityManager entityManager;

    public ReservationPersistenceAdapter(SpringDataJpaReservationRepository repository, EntityManager entityManager) {
        this.repository = repository;
        this.entityManager = entityManager;
    }

    @Override
    public Reservation save(Reservation reservation) {
        return repository.save(reservation);
    }

    @Override
    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    @Override
    public Optional<Reservation> findById(Long id) {
        return repository.findById(id);
    }

    @Override
    public Optional<Reservation> findByDateAndTimeIdAndThemeId(LocalDate date, Long timeId, Long themeId) {
        return repository.findByDateAndTimeIdAndThemeId(date, timeId, themeId);
    }

    @Override
    public List<Long> findThemeReservationCountsForDate(LocalDate startDate, LocalDate endDate) {
        PageRequest limitTen = PageRequest.of(0, 10);
        return repository.findThemeReservationCountsForDate(startDate, endDate, limitTen);
    }

    @Override
    public List<Reservation> findAll() {
        return repository.findAll();
    }

    @Override
    public List<Reservation> findByMemberAndThemeAndDateRange(Long memberId, Long themeId, LocalDate dateFrom, LocalDate dateTo) {
        String baseJpql = """
                SELECT r
                FROM Reservation AS r
                JOIN FETCH r.member
                JOIN FETCH r.time
                JOIN FETCH r.theme
                WHERE 1 = 1
                """;
        StringBuilder dynamicJpql = new StringBuilder(baseJpql);
        HashMap<String, Object> parameters = new HashMap<>();
        if (memberId != null) {
            dynamicJpql.append(" AND r.member.id = :memberId");
            parameters.put("memberId", memberId);
        }

        if (themeId != null) {
            dynamicJpql.append(" AND r.theme.id = :themeId");
            parameters.put("themeId", themeId);
        }

        if (dateFrom != null && dateTo != null) {
            dynamicJpql.append(" AND r.date BETWEEN :dateFrom AND :dateTo");
            parameters.put("dateFrom", dateFrom);
            parameters.put("dateTo", dateTo);
        }

        TypedQuery<Reservation> query = entityManager.createQuery(dynamicJpql.toString(), Reservation.class);
        parameters.forEach(query::setParameter);

        return query.getResultList();
    }

    @Override
    public List<Reservation> findByDateAndThemeId(LocalDate date, Long themeId) {
        return repository.findByDateAndThemeId(date, themeId);
    }

    @Override
    public List<Reservation> findAllByMember(Member member) {
        return repository.findAllByMember(member);
    }

    @Override
    public boolean existsByTimeId(Long id) {
        return repository.existsByTimeId(id);
    }

    @Override
    public boolean existsByThemeId(Long id) {
        return repository.existsByThemeId(id);
    }

    @Override
    public boolean existsByDateAndTimeIdAndThemeId(LocalDate date, Long timeId, Long themeId) {
        return repository.existsByDateAndTimeIdAndThemeId(date, timeId, themeId);
    }
}
