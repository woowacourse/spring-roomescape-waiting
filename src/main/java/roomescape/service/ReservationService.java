package roomescape.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.dto.business.ReservationCreationContent;
import roomescape.dto.response.ReservationResponse;
import roomescape.exception.local.DuplicateReservationException;
import roomescape.exception.local.NotFoundMemberException;
import roomescape.exception.local.NotFoundReservationException;
import roomescape.exception.local.NotFoundReservationTimeException;
import roomescape.exception.local.NotFoundThemeException;
import roomescape.exception.local.PastReservationCreationException;
import roomescape.repository.MemberRepository;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;

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
            ThemeRepository themeRepository, MemberRepository memberRepository
    ) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.memberRepository = memberRepository;
    }

    public List<ReservationResponse> findAllReservations() {
        List<Reservation> reservations = reservationRepository.findAll();
        return reservations.stream()
                .map(ReservationResponse::new)
                .toList();
    }

    public List<ReservationResponse> findAllReservationsByMember(long memberId) {
        Member savedMember = getMemberById(memberId);
        List<Reservation> reservations = reservationRepository.findByMember(savedMember);
        return reservations.stream()
                .map(ReservationResponse::new)
                .toList();
    }

    public List<ReservationResponse> findReservationsByFilter(
            long memberId, long themeId, LocalDate from, LocalDate to
    ) {
        List<Reservation> reservations = reservationRepository.findReservationsByFilter(memberId, themeId, from, to);
        return reservations.stream()
                .map(ReservationResponse::new)
                .toList();
    }

    public ReservationResponse addReservation(long memberId, ReservationCreationContent request) {
        Member member = getMemberById(memberId);
        Theme theme = getThemeById(request.themeId());
        ReservationTime time = getReservationTimeById(request.timeId());

        Reservation reservation = Reservation.createWithoutId(
                request.date(), ReservationStatus.BOOKED, time, theme, member);

        validateDuplicateReservation(theme, request.date(), time);
        validatePastReservationCreation(reservation);

        Reservation savedReservation = reservationRepository.save(reservation);
        return new ReservationResponse(savedReservation);
    }

    public void deleteReservationById(long reservationId) {
        Reservation reservation = getReservationById(reservationId);
        reservationRepository.delete(reservation);
    }

    private void validateDuplicateReservation(Theme theme, LocalDate date, ReservationTime time) {
        boolean isDuplicatedReservation =
                reservationRepository.existsByThemeAndDateAndReservationTime(theme, date, time);
        if (isDuplicatedReservation) {
            throw new DuplicateReservationException();
        }
    }

    private void validatePastReservationCreation(Reservation reservation) {
        if (reservation.isPastDateTime()) {
            throw new PastReservationCreationException();
        }
    }

    private Member getMemberById(long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(NotFoundMemberException::new);
    }

    private Theme getThemeById(long themeId) {
        return themeRepository.findById(themeId)
                .orElseThrow(NotFoundThemeException::new);
    }

    private ReservationTime getReservationTimeById(long timeId) {
        return reservationTimeRepository.findById(timeId)
                .orElseThrow(NotFoundReservationTimeException::new);
    }

    private Reservation getReservationById(long reservationId) {
        return reservationRepository.findById(reservationId)
                .orElseThrow(NotFoundReservationException::new);
    }
}
