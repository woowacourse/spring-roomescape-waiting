package roomescape.reservation.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.member.model.Member;
import roomescape.member.service.MemberService;
import roomescape.reservation.dto.ReservationDto;
import roomescape.reservation.dto.SaveReservationRequest;
import roomescape.reservation.dto.SearchReservationsParams;
import roomescape.reservation.dto.SearchReservationsRequest;
import roomescape.reservation.model.Reservation;
import roomescape.reservation.model.ReservationDate;
import roomescape.reservation.model.ReservationStatus;
import roomescape.reservation.model.ReservationTime;
import roomescape.reservation.model.ReservationWaiting;
import roomescape.reservation.model.Theme;
import roomescape.reservation.repository.CustomReservationRepository;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.repository.ReservationWaitingRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@Transactional
@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationWaitingRepository reservationWaitingRepository;
    private final CustomReservationRepository customReservationRepository;
    private final MemberService memberService;
    private final ThemeService themeService;
    private final ReservationTimeService reservationTimeService;

    public ReservationService(
            final CustomReservationRepository customReservationRepository,
            final ReservationRepository reservationRepository,
            final ReservationWaitingRepository reservationWaitingRepository,
            final MemberService memberService,
            final ThemeService themeService,
            final ReservationTimeService reservationTimeService
    ) {
        this.customReservationRepository = customReservationRepository;
        this.reservationRepository = reservationRepository;
        this.reservationWaitingRepository = reservationWaitingRepository;
        this.memberService = memberService;
        this.themeService = themeService;
        this.reservationTimeService = reservationTimeService;
    }

    @Transactional(readOnly = true)
    public List<ReservationDto> getReservations() {
        return reservationRepository.findAll()
                .stream()
                .map(ReservationDto::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReservationDto> searchReservations(final SearchReservationsRequest request) {
        final SearchReservationsParams searchReservationsParams = new SearchReservationsParams(
                request.memberId(),
                request.themeId(),
                request.from(),
                request.to()
        );

        return customReservationRepository.searchReservations(searchReservationsParams)
                .stream()
                .map(ReservationDto::from)
                .toList();
    }

    public ReservationDto saveReservation(final SaveReservationRequest request) {
        final ReservationTime reservationTime = reservationTimeService.getReservationTime(request.timeId());
        final Theme theme = themeService.getTheme(request.themeId());
        final Member member = memberService.getMember(request.memberId());
        final Reservation reservation = request.toReservation(reservationTime, theme, member);

        validateReservationDateAndTime(reservation.getDate(), reservationTime);
        validateReservationDuplicated(reservation);

        return ReservationDto.from(reservationRepository.save(reservation));
    }

    private static void validateReservationDateAndTime(final ReservationDate date, final ReservationTime time) {
        final LocalDateTime reservationLocalDateTime = LocalDateTime.of(date.getValue(), time.getStartAt());
        if (reservationLocalDateTime.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("현재 날짜보다 이전 날짜를 예약할 수 없습니다.");
        }
    }

    private void validateReservationDuplicated(final Reservation reservation) {
        if (reservationRepository.existsByDateAndTime_IdAndTheme_Id(
                reservation.getDate(),
                reservation.getTime().getId(),
                reservation.getTheme().getId())
        ) {
            throw new IllegalArgumentException("이미 해당 날짜/시간의 테마 예약이 있습니다.");
        }
    }

    public void deleteReservation(final Long reservationId) {
        final Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 예약 정보입니다."));
        reservationRepository.deleteById(reservation.getId());
        reservationWaitingRepository.findTopByDateAndTimeAndThemeOrderByCreatedAtAsc(
                reservation.getDate(),
                reservation.getTime(),
                reservation.getTheme())
                .ifPresent(this::saveReservationWithWaiting);
    }

    private void saveReservationWithWaiting(final ReservationWaiting reservationWaiting) {
        final Reservation reservation = new Reservation(
                ReservationStatus.RESERVATION,
                reservationWaiting.getDate().getValue(),
                reservationWaiting.getTime(),
                reservationWaiting.getTheme(),
                reservationWaiting.getMember()
        );
        reservationRepository.save(reservation);
        reservationWaitingRepository.delete(reservationWaiting);
    }

    @Transactional(readOnly = true)
    public List<ReservationDto> getMyReservations(final Long memberId) {
        return reservationRepository.findAllByMember_Id(memberId)
                .stream()
                .map(ReservationDto::from)
                .toList();
    }
}
