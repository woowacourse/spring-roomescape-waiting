package roomescape.infrastructure;

import static roomescape.infrastructure.ReservationSpecification.dateEqualOrAfter;
import static roomescape.infrastructure.ReservationSpecification.dateEqualOrBefore;
import static roomescape.infrastructure.ReservationSpecification.memberIdEqual;
import static roomescape.infrastructure.ReservationSpecification.themeIdEqual;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.repository.ReservationRepository;
import roomescape.dto.request.ReservationCondition;

@Repository
public class ReservationRepositoryAdaptor implements ReservationRepository {
    private final JpaReservationRepository jpaReservationRepository;

    public ReservationRepositoryAdaptor(JpaReservationRepository jpaReservationRepository) {
        this.jpaReservationRepository = jpaReservationRepository;
    }

    @Override
    public List<Reservation> findAll() {
        return jpaReservationRepository.findAll();
    }

    @Override
    public Reservation save(Reservation reservation) {
        return jpaReservationRepository.save(reservation);
    }

    @Override
    public void deleteById(Long id) {
        jpaReservationRepository.deleteById(id);
    }

    @Override
    public List<Reservation> findByReservationTimeId(Long id) {
        return jpaReservationRepository.findByReservationTimeId(id);
    }

    @Override
    public Optional<Reservation> findById(Long id) {
        return jpaReservationRepository.findById(id);
    }

    @Override
    public Optional<Reservation> findByDateAndReservationTimeAndTheme(LocalDate date, ReservationTime time,
                                                                      Theme theme) {
        return jpaReservationRepository.findByDateAndReservationTimeAndTheme(date, time, theme);
    }

    @Override
    public List<Reservation> findByDateBetween(LocalDate dateFrom, LocalDate dateTo) {
        return jpaReservationRepository.findByDateBetween(dateFrom, dateTo);
    }

    @Override
    public List<Reservation> findByDateAndTheme(LocalDate date, Theme theme) {
        return jpaReservationRepository.findByDateAndTheme(date, theme);
    }

    @Override
    public List<Reservation> findByCondition(ReservationCondition condition) {
        Specification<Reservation> spec =
                Specification.where(themeIdEqual(condition.themeId()))
                        .and(memberIdEqual(condition.memberId())
                                .and(dateEqualOrAfter(condition.dateFrom())))
                        .and(dateEqualOrBefore(condition.dateTo()));

        return jpaReservationRepository.findAll(spec);
    }

    @Override
    public List<Reservation> findByThemeId(Long themeId) {
        return jpaReservationRepository.findByThemeId(themeId);
    }

    @Override
    public List<Reservation> findByMemberId(Long memberId) {
        return jpaReservationRepository.findByMemberId(memberId);
    }
}
