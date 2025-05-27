package roomescape.reservation.repository.impl;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationDate;
import roomescape.reservation.repository.JpaReservationRepository;
import roomescape.reservation.repository.ReservationRepository;

@Repository
@RequiredArgsConstructor
// TODO : 구현체 이름 고민해보기
public class ReservationRepositoryImpl implements ReservationRepository {

    private final JpaReservationRepository jpaReservationRepository;

    @Override
    public boolean existsByTimeId(Long timeId) {
        return jpaReservationRepository.existsByTimeId(timeId);
    }

    @Override
    public boolean existsByParams(ReservationDate date, Long timeId, Long themeId) {
        return jpaReservationRepository.existsByDateAndTimeIdAndThemeId(date, timeId, themeId);
    }

    @Override
    public boolean existsByParams(ReservationDate date, Long timeId, Long themeId, Long memberId) {
        return jpaReservationRepository.existsByDateAndTimeIdAndThemeIdAndMemberId(date, timeId, themeId, memberId);
    }

    @Override
    public List<Reservation> findByParams(Long memberId, Long themeId, ReservationDate from,
                                          ReservationDate to) {
        return jpaReservationRepository.findByMemberIdAndThemeIdAndDateBetween(memberId, themeId, from, to);
    }

    @Override
    public List<Reservation> findByParams(ReservationDate date, Long themeId) {
        return jpaReservationRepository.findByDateAndThemeId(date, themeId);
    }

    @Override
    public List<Reservation> findAllByMemberId(Long memberId) {
        return jpaReservationRepository.findAllByMemberId(memberId);
    }

    @Override
    public Reservation save(Reservation reservation) {
        return jpaReservationRepository.save(reservation);
    }

    @Override
    public List<Reservation> findAll() {
        return jpaReservationRepository.findAll();
    }

    @Override
    public void deleteById(Long id) {
        jpaReservationRepository.deleteById(id);
    }

    @Override
    public Optional<Reservation> findById(Long id) {
        return jpaReservationRepository.findById(id);
    }
}
