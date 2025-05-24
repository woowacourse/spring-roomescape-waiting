package roomescape.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.Waiting;
import roomescape.domain.repository.MemberRepository;
import roomescape.domain.repository.ReservationRepository;
import roomescape.domain.repository.ReservationTimeRepository;
import roomescape.domain.repository.ThemeRepository;
import roomescape.domain.repository.WaitingRepository;
import roomescape.dto.request.ReservationCondition;
import roomescape.dto.response.ReservationResponse;
import roomescape.dto.response.ReservationWithStatusResponse;
import roomescape.dto.response.WaitingResponse;
import roomescape.exception.ExistedReservationException;
import roomescape.exception.MemberNotFoundException;
import roomescape.exception.ReservationNotFoundException;
import roomescape.exception.ReservationTimeNotFoundException;
import roomescape.exception.ThemeNotFoundException;

@Service
@Transactional
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;
    private final WaitingRepository waitingRepository;

    public ReservationService(final ReservationRepository reservationRepository,
                              final ReservationTimeRepository reservationTimeRepository,
                              final ThemeRepository themeRepository,
                              final MemberRepository memberRepository, final WaitingRepository waitingRepository) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.memberRepository = memberRepository;
        this.waitingRepository = waitingRepository;
    }

    @Transactional(readOnly = true)
    public List<ReservationResponse> findReservations(ReservationCondition cond) {
        List<Reservation> filteredReservations = reservationRepository.findByCondition(cond);
        return filteredReservations.stream()
                .map(ReservationResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReservationWithStatusResponse> findBookingHistory(Long memberId) {
        List<ReservationWithStatusResponse> reservations = findReservationByMemberId(memberId);
        List<ReservationWithStatusResponse> waitings = findWaitingByMemberId(memberId);
        return Stream.concat(reservations.stream(), waitings.stream())
                .toList();
    }

    private List<ReservationWithStatusResponse> findReservationByMemberId(Long memberId) {
        return reservationRepository.findByMemberId(memberId).stream()
                .map(ReservationWithStatusResponse::from)
                .toList();
    }

    private List<ReservationWithStatusResponse> findWaitingByMemberId(Long memberId) {
        return waitingRepository.findByMemberId(memberId).stream()
                .map(ReservationWithStatusResponse::from)
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

    public WaitingResponse createWaiting(Long memberId, Long timeId, Long themeId, LocalDate date,
                                         LocalDateTime createAt) {
        ReservationTime reservationTime = reservationTimeRepository.findById(timeId)
                .orElseThrow(ReservationTimeNotFoundException::new);
        Theme theme = themeRepository.findById(themeId).orElseThrow(ThemeNotFoundException::new);
        Member member = memberRepository.findById(memberId).orElseThrow(MemberNotFoundException::new);

        Waiting waiting = Waiting.createWithoutId(member, date, reservationTime, theme, createAt);

        waiting.validateDateTime();
        validateWaitingDuplicate(date, reservationTime, theme, member);

        Waiting savedWaiting = waitingRepository.save(waiting);
        return WaitingResponse.from(savedWaiting);
    }

    private void validateDuplicate(LocalDate date, ReservationTime time, Theme theme) {
        if (reservationRepository.findByDateAndReservationTimeAndTheme(date, time, theme).isPresent()) {
            throw new ExistedReservationException();
        }
    }

    private void validateWaitingDuplicate(LocalDate date, ReservationTime time, Theme theme, Member member) {
        if (waitingRepository.findByDateAndReservationTimeAndThemeAndMember(date, time, theme, member).isPresent()) {
            throw new ExistedReservationException();
        }
    }

    public void deleteReservationById(Long id) {
        reservationRepository.findById(id).orElseThrow(ReservationNotFoundException::new);
        reservationRepository.deleteById(id);
    }

    public void deleteWaitingById(Long id) {
        waitingRepository.findById(id).orElseThrow(ReservationNotFoundException::new);
        waitingRepository.deleteById(id);
    }
}
