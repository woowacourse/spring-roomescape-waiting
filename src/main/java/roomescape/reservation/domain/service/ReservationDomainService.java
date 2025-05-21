package roomescape.reservation.domain.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.common.security.dto.request.MemberInfo;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.presentation.dto.response.MyReservationResponse;
import roomescape.reservation.exception.ReservationAlreadyExistsException;
import roomescape.reservation.domain.repository.ReservationRepository;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.presentation.dto.response.AvailableReservationTimeResponse;
import roomescape.theme.domain.Theme;

@Service
public class ReservationDomainService {

    private final ReservationRepository reservationRepository;

    public ReservationDomainService(final ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    public void delete(Long id) {
        reservationRepository.deleteById(id);
    }

    public void checkIfReservationExists(final LocalDate date, final Long timeId, final Long themeId) {
        if (reservationRepository.existsByDateAndTimeIdAndThemeId(date, timeId, themeId)) {
            throw new ReservationAlreadyExistsException("해당 시간에 이미 예약이 존재합니다.");
        }
    }

    public List<Reservation> getReservations(final Long themeId, final Long memberId,
                                             final LocalDate startDate, final LocalDate endDate) {
        if ((themeId == null) || (memberId == null) || (startDate == null) || (endDate == null)) {
            return reservationRepository.findAll();
        }
        return reservationRepository.findFilteredReservations(themeId, memberId, startDate, endDate);
    }

    public Reservation save(final Member member, final LocalDate date, final ReservationTime time, final Theme theme,
                            final LocalDateTime now) {
        return reservationRepository.save(
                Reservation.createUpcomingReservationWithUnassignedId(member, date, time, theme, now,
                        ReservationStatus.RESERVED));
    }

    public List<MyReservationResponse> findMyReservations(final MemberInfo memberInfo) {
        return reservationRepository.findByMemberId(memberInfo.id())
                .stream()
                .map(MyReservationResponse::from)
                .toList();
    }

    public boolean existsByTimeId(final Long timeId) {
        return reservationRepository.existsByTimeId(timeId);
    }

    public List<AvailableReservationTimeResponse> findBookedTimesByDateAndThemeId(final LocalDate date,
                                                                                  final Long themeId) {
        return reservationRepository.findBookedTimesByDateAndThemeId(date, themeId);
    }

    public boolean existsByThemeId(final Long themeId) {
        return reservationRepository.existsByThemeId(themeId);
    }
}
