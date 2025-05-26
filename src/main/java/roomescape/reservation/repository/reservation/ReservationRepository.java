package roomescape.reservation.repository.reservation;

import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;
import roomescape.common.exception.DataNotFoundException;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationTime;
import roomescape.theme.domain.Theme;

@RequiredArgsConstructor
@Repository
public class ReservationRepository implements ReservationRepositoryInterface {

    private final JpaReservationRepository jpaReservationRepository;

    @Override
    public List<Reservation> findAll() {
        return jpaReservationRepository.findAll();
    }

    @Override
    public boolean existsByDateAndTimeAndTheme(final LocalDate date, final ReservationTime time, final Theme theme) {
        return jpaReservationRepository.existsByDateAndTimeAndTheme(date, time, theme);
    }

    @Override
    public List<Theme> findPopularThemesByReservationBetween(
            LocalDate dateFrom, LocalDate dateTo, PageRequest pageRequest) {
        return jpaReservationRepository.findPopularThemesByReservationBetween(dateFrom, dateTo, pageRequest);
    }

    @Override
    public List<Reservation> findByMember(Member member) {
        return jpaReservationRepository.findByMember(member);
    }

    @Override
    public Reservation findById(Long id) {
        return jpaReservationRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("해당 예약 데이터가 존재하지 않습니다. id = " + id));
    }

    @Override
    public Reservation save(final Reservation reservation) {
        return jpaReservationRepository.save(reservation);
    }

    @Override
    public void deleteById(Long id) {
        jpaReservationRepository.deleteById(id);
    }

    @Override
    public List<Reservation> findByThemeAndMemberAndDateBetween(
            final Theme theme,
            final Member member,
            final LocalDate dateFrom,
            final LocalDate dateTo) {
        return jpaReservationRepository.findByThemeAndMemberAndDateBetween(theme, member, dateFrom, dateTo);
    }
}
