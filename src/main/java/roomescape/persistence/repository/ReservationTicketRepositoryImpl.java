package roomescape.persistence.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import roomescape.common.exception.NotFoundException;
import roomescape.infrastructure.db.ReservationTicketJpaRepository;
import roomescape.model.ReservationSpec;
import roomescape.model.ReservationTicket;
import roomescape.persistence.vo.Period;

@Repository
@RequiredArgsConstructor
public class ReservationTicketRepositoryImpl implements ReservationTicketRepository {

    private final ReservationTicketJpaRepository reservationTicketJpaRepository;

    @Override
    public boolean isDuplicated(ReservationSpec reservationSpec) {
        return reservationTicketJpaRepository.findByReservationSpec_DateAndReservationSpec_ReservationTimeAndReservationSpec_Theme(
                        reservationSpec.getDate(),
                        reservationSpec.getReservationTime(),
                        reservationSpec.getTheme()
                )
                .isPresent();
    }

    @Override
    public List<ReservationTicket> findForThemeAndMemberInPeriod(Long themeId, Long memberId, Period period) {
        return reservationTicketJpaRepository.findByReservationSpec_ThemeIdAndReservationSpec_MemberIdAndReservationSpec_DateBetween(
                themeId,
                memberId,
                period.startDate(),
                period.endDate()
        );
    }

    @Override
    public List<ReservationTicket> findForThemeOnDate(Long themeId, LocalDate date) {
        return reservationTicketJpaRepository.findByReservationSpec_ThemeIdAndReservationSpec_Date(themeId, date);
    }

    @Override
    public List<ReservationTicket> findForMember(Long memberId) {
        return reservationTicketJpaRepository.findByReservationSpec_MemberId(memberId);
    }

    @Override
    public ReservationTicket save(ReservationTicket reservationTicket) {
        return reservationTicketJpaRepository.save(reservationTicket);
    }

    @Override
    public List<ReservationTicket> findAll() {
        return reservationTicketJpaRepository.findAll();
    }

    @Override
    public void deleteById(Long id) {
        reservationTicketJpaRepository.deleteById(id);
    }

    @Override
    public ReservationTicket findById(Long id) {
        return reservationTicketJpaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("해당 예약 내역을 찾을 수 없습니다."));
    }

    @Override
    public Optional<ReservationTicket> findForThemeAndReservationTimeOnDate(
            ReservationSpec reservationSpec
    ) {
        return reservationTicketJpaRepository.findByReservationSpec_ThemeAndReservationSpec_ReservationTimeAndReservationSpec_Date(
                reservationSpec.getTheme(),
                reservationSpec.getReservationTime(),
                reservationSpec.getDate()
        );
    }
}
