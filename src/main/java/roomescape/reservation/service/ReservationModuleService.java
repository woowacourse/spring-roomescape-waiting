package roomescape.reservation.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.global.auth.dto.UserInfo;
import roomescape.member.domain.Member;
import roomescape.member.exception.MemberNotFoundException;
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationInfo;
import roomescape.reservation.dto.response.ReservationResponse;
import roomescape.reservation.exception.ReservationAlreadyExistsException;
import roomescape.reservation.exception.ReservationNotFoundException;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.repository.ReservationTimeRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.repository.ThemeRepository;

@Service
public class ReservationModuleService {

    private final ReservationRepository reservationRepository;

    public ReservationModuleService(final ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    public List<ReservationResponse> findReservations(final Long themeId, final Long memberId,
                                                      final LocalDate startDate,
                                                      final LocalDate endDate) {
        return getReservations(themeId, memberId, startDate, endDate)
                .stream()
                .map(reservation -> {
                    ReservationTime time = reservation.getInfo().getTime();
                    Theme theme = reservation.getInfo().getTheme();
                    Member member = reservation.getMember();
                    return ReservationResponse.of(reservation, time, theme, member);
                })
                .toList();
    }
    public List<Reservation> getReservations(final Long themeId, final Long memberId,
                                             final LocalDate startDate, final LocalDate endDate) {
        if ((themeId == null) || (memberId == null) || (startDate == null) || (endDate == null)) {
            return reservationRepository.findAll();
        }
        return reservationRepository.findFilteredReservations(themeId, memberId, startDate, endDate);
    }

    public void delete(Long id) {
        reservationRepository.deleteById(id);
    }

    public void checkIfReservationExists(final LocalDate date, final Long timeId, final Long themeId) {
        boolean exists = isReservationExists(date, timeId, themeId);
        if (exists) {
            throw new ReservationAlreadyExistsException("해당 시간에 이미 예약이 존재합니다.");
        }
    }

    public boolean isReservationExists(final LocalDate date, final Long timeId, final Long themeId) {
        if (reservationRepository.existsByDateAndTimeIdAndThemeId(date, timeId, themeId)) {
            return true;
        }
        return false;
    }

    public List<Reservation> findMyReservations(final UserInfo userInfo) {
        return reservationRepository.findByMemberId(userInfo.id());
    }

    public Reservation save(final Reservation reservation) {
        return reservationRepository.save(reservation);
    }

    public Reservation findById(final Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new ReservationNotFoundException("요청한 id와 일치하는 예약 정보가 없습니다."));
    }
}
