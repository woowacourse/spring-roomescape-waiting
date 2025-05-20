package roomescape.reservation.service;

import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.exception.ExistedReservationException;
import roomescape.exception.MemberNotFoundException;
import roomescape.exception.ReservationNotFoundException;
import roomescape.exception.ReservationTimeNotFoundException;
import roomescape.exception.ThemeNotFoundException;
import roomescape.member.domain.Member;
import roomescape.member.infrastructure.MemberRepository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.Theme;
import roomescape.reservation.dto.request.ReservationCondition;
import roomescape.reservation.dto.response.ReservationResponse;
import roomescape.reservation.dto.response.ReservationWithStatusResponse;
import roomescape.reservation.infrastructure.ReservationRepository;
import roomescape.reservation.infrastructure.ReservationTimeRepository;
import roomescape.reservation.infrastructure.ThemeRepository;

@Service
@Transactional
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;

    public ReservationService(
            ReservationRepository reservationRepository,
            ReservationTimeRepository reservationTimeRepository,
            ThemeRepository themeRepository,
            MemberRepository memberRepository
    ) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.memberRepository = memberRepository;
    }

    public List<ReservationResponse> findReservations(ReservationCondition cond) {
        List<Reservation> filteredReservations = reservationRepository.findByCondition(cond);
        return filteredReservations.stream()
                .map(ReservationResponse::from)
                .toList();
    }

    public ReservationResponse createReservation(Long memberId, Long timeId, Long themeId, LocalDate date) {
        ReservationTime reservationTime = reservationTimeRepository.findById(timeId)
                .orElseThrow(ReservationTimeNotFoundException::new);
        Theme theme = themeRepository.findById(themeId).orElseThrow(ThemeNotFoundException::new);
        Member member = memberRepository.findById(memberId).orElseThrow(MemberNotFoundException::new);
        Reservation reservation = Reservation.createWithoutId(member, date, reservationTime, theme);
        reservation.validateDateTime();
        validateDuplicate(date, reservationTime, theme);
        Reservation savedReservation = reservationRepository.save(reservation);
        return ReservationResponse.from(savedReservation);
    }

    private void validateDuplicate(LocalDate date, ReservationTime time, Theme theme) {
        if (reservationRepository.findByDateAndReservationTimeAndTheme(date, time, theme).isPresent()) {
            throw new ExistedReservationException();
        }
    }

    public void deleteReservationById(Long id) {
        reservationRepository.findById(id).orElseThrow(ReservationNotFoundException::new);
        reservationRepository.deleteById(id);
    }

    public List<ReservationWithStatusResponse> findReservationByMemberId(Long memberId) {
        return reservationRepository.findByMemberId(memberId).stream()
                .map(ReservationWithStatusResponse::from)
                .toList();
    }
}
