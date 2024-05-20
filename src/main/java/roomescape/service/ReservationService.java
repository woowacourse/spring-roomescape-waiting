package roomescape.service;

import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationDetail;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.repository.MemberRepository;
import roomescape.domain.repository.ReservationDetailRepository;
import roomescape.domain.repository.ReservationRepository;
import roomescape.domain.repository.ReservationTimeRepository;
import roomescape.domain.repository.ThemeRepository;
import roomescape.exception.reservation.DuplicatedReservationException;
import roomescape.exception.reservation.InvalidDateTimeReservationException;
import roomescape.exception.reservation.NotFoundReservationException;
import roomescape.exception.theme.NotFoundThemeException;
import roomescape.exception.time.NotFoundTimeException;
import roomescape.service.dto.request.reservation.ReservationRequest;
import roomescape.service.dto.request.reservation.ReservationSearchCond;
import roomescape.service.dto.response.reservation.ReservationResponse;
import roomescape.service.dto.response.reservation.UserReservationResponse;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReservationService {
    private final ReservationDetailRepository reservationDetailRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ReservationRepository reservationRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;

    public List<ReservationResponse> findAllReservation() {
        List<Reservation> reservations = reservationRepository.findAll();
        return reservations.stream()
                .map(ReservationResponse::from)
                .toList();
    }

    public List<ReservationResponse> findAllReservationByConditions(ReservationSearchCond cond) {
        List<Reservation> reservations = reservationRepository.findByPeriodAndThemeAndMember(
                cond.start(), cond.end(), cond.themeId(), cond.memberId());

        return reservations.stream()
                .map(ReservationResponse::from)
                .toList();
    }

    public List<UserReservationResponse> findAllByMemberId(Long memberId) {
        return null;
    }

    @Transactional
    public Reservation saveReservation(ReservationRequest request) {
        return new Reservation(null, null, null);
    }

    private void validateDuplicateReservation(ReservationRequest request) {
        if (reservationDetailRepository.existsByDateAndTimeIdAndThemeId(
                request.date(), request.timeId(), request.themeId())) {
            throw new DuplicatedReservationException();
        }
    }

    private void validateDateTimeReservation(ReservationRequest request, ReservationTime time) {
        LocalDateTime localDateTime = request.date().atTime(time.getStartAt());
        if (localDateTime.isBefore(LocalDateTime.now())) {
            throw new InvalidDateTimeReservationException();
        }
    }

    @Transactional
    public void deleteReservation(Long id) {
        ReservationDetail reservationDetail = getReservationById(id);
        reservationDetailRepository.delete(reservationDetail);
    }

    private ReservationDetail getReservationById(Long id) {
        return reservationDetailRepository.findById(id)
                .orElseThrow(NotFoundReservationException::new);
    }

    private ReservationTime findReservationTimeById(Long id) {
        return reservationTimeRepository.findById(id)
                .orElseThrow(NotFoundTimeException::new);
    }

    private Theme findThemeById(Long id) {
        return themeRepository.findById(id)
                .orElseThrow(NotFoundThemeException::new);
    }
}
