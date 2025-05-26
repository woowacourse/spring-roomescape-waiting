package roomescape.reservationslot.domain.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.member.domain.Member;
import roomescape.reservationslot.domain.ReservationSlot;
import roomescape.reservationslot.domain.repository.ReservationSlotRepository;
import roomescape.reservationslot.exception.ReservationSlotAlreadyExistsException;
import roomescape.reservationslot.exception.ReservationSlotNotFoundException;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.presentation.dto.response.AvailableReservationTimeResponse;
import roomescape.theme.domain.Theme;

@Service
public class ReservationSlotDomainService {

    private final ReservationSlotRepository reservationSlotRepository;

    public ReservationSlotDomainService(final ReservationSlotRepository reservationSlotRepository) {
        this.reservationSlotRepository = reservationSlotRepository;
    }

    public void delete(Long id) {
        reservationSlotRepository.deleteById(id);
    }

    public void checkIfReservationDoesNotExists(final LocalDate date, final Long timeId, final Long themeId) {
        if (reservationSlotRepository.existsByDateAndTimeIdAndThemeId(date, timeId, themeId)) {
            throw new ReservationSlotAlreadyExistsException("해당 시간에 이미 예약이 존재합니다.");
        }
    }

    public ReservationSlot getReservationByDateAndTimeAndTheme(final LocalDate date, final Long timeId,
                                                               final Long themeId) {
        return reservationSlotRepository.findByDateAndTimeIdAndThemeId(date, timeId, themeId)
                .orElseThrow(() -> new ReservationSlotNotFoundException("해당 시간에 예약이 존재하지 않습니다."));
    }

    public List<ReservationSlot> findFilteredReservations(final Long themeId, final Long memberId,
                                                          final LocalDate startDate, final LocalDate endDate) {
        return reservationSlotRepository.findByThemeIdAndDateBetweenAndReservationMemberId(themeId, startDate, endDate,
                memberId);
    }

    public ReservationSlot save(final Member member, final LocalDate date, final ReservationTime time,
                                final Theme theme, final LocalDateTime now) {
        return reservationSlotRepository.save(
                ReservationSlot.createUpcomingReservation(member, date, time, theme, now));
    }

    public boolean existsByTimeId(final Long timeId) {
        return reservationSlotRepository.existsByTimeId(timeId);
    }

    public List<AvailableReservationTimeResponse> findBookedTimesByDateAndThemeId(final LocalDate date,
                                                                                  final Long themeId) {
        return reservationSlotRepository.findBookedTimesByDateAndThemeId(date, themeId);
    }

    public boolean existsByThemeId(final Long themeId) {
        return reservationSlotRepository.existsByThemeId(themeId);
    }
}
