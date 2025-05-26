package roomescape.reservation.repository;

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
public class JpaReservationRepository implements ReservationRepositoryInterface {

    private final ReservationRepository reservationRepository;

    @Override
    public List<Reservation> findAll() {
        return reservationRepository.findAll();
    }

    @Override
    public boolean existsByDateAndTimeAndTheme(final LocalDate date, final ReservationTime time, final Theme theme) {
        return reservationRepository.existsByDateAndTimeAndTheme(date, time, theme);
    }

    @Override
    public List<Theme> findPopularThemesByReservationBetween(
            LocalDate dateFrom, LocalDate dateTo, PageRequest pageRequest) {
        return reservationRepository.findPopularThemesByReservationBetween(dateFrom, dateTo, pageRequest);
    }

    @Override
    public List<Reservation> findByMember(Member member) {
        return reservationRepository.findByMember(member);
    }

    @Override
    public Reservation findById(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("해당 예약 데이터가 존재하지 않습니다. id = " + id));
    }

    @Override
    public Reservation save(final Reservation reservation) {
        return reservationRepository.save(reservation);
    }

    @Override
    public void deleteById(Long id) {
        reservationRepository.deleteById(id);
    }

    @Override
    public List<Reservation> findByThemeAndMemberAndDateBetween(
            final Theme theme,
            final Member member,
            final LocalDate dateFrom,
            final LocalDate dateTo) {
        return reservationRepository.findByThemeAndMemberAndDateBetween(theme, member, dateFrom, dateTo);
    }
}
