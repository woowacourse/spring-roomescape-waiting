package roomescape.reservation.application;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.admin.AdminReservationRequest;
import roomescape.admin.AdminWaitingReservationResponse;
import roomescape.member.application.repository.MemberRepository;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.presentation.dto.ReservationRequest;
import roomescape.reservation.presentation.dto.ReservationResponse;
import roomescape.reservation.presentation.dto.UserReservationsResponse;
import roomescape.reservation.time.application.ReservationTimeRepository;
import roomescape.reservation.time.domain.ReservationTime;
import roomescape.theme.application.ThemeRepository;
import roomescape.theme.domain.Theme;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;

    public ReservationService(final ReservationRepository reservationRepository,
                              final ReservationTimeRepository reservationTimeRepository,
                              final ThemeRepository themeRepository,
                              final MemberRepository memberRepository) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.memberRepository = memberRepository;
    }

    @Transactional
    public ReservationResponse createReservation(final ReservationRequest reservationRequest, final Long memberId) {
        ReservationTime reservationTime = getReservationTime(reservationRequest.getTimeId());
        Theme theme = getTheme(reservationRequest.getThemeId());
        final LocalDate date = reservationRequest.getDate();
        validateReservationDateTime(date, reservationTime);

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new NoSuchElementException("유저 정보를 찾을 수 없습니다."));

        final Reservation reservation = Reservation.createReserved(
                member,
                theme,
                date,
                reservationTime
        );

        return new ReservationResponse(reservationRepository.save(reservation));
    }

    public ReservationResponse createWaitingReservation(final ReservationRequest request, final Long memberId) {
        ReservationTime reservationTime = getReservationTime(request.getTimeId());
        Theme theme = getTheme(request.getThemeId());
        LocalDate date = request.getDate();
        validateReservationDateTime(date, reservationTime);

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new NoSuchElementException("유저 정보를 찾을 수 없습니다."));

        final Reservation reservation = Reservation.createWaiting(
                member,
                theme,
                date,
                reservationTime
        );

        return new ReservationResponse(reservationRepository.save(reservation));
    }

    @Transactional
    public ReservationResponse createReservation(final AdminReservationRequest adminReservationRequest) {
        ReservationTime reservationTime = getReservationTime(adminReservationRequest.getTimeId());
        Theme theme = getTheme(adminReservationRequest.getThemeId());
        final LocalDate date = adminReservationRequest.getDate();
        validateReservationDateTime(date, reservationTime);

        Member member = findMemberById(adminReservationRequest.getMemberId());

        final Reservation reservation = Reservation.createReserved(
                member,
                theme,
                date,
                reservationTime
        );

        return new ReservationResponse(reservationRepository.save(reservation));
    }

    public List<ReservationResponse> getReservations(Long memberId, Long themeId, LocalDate dateFrom,
                                                     LocalDate dateTo) {

        if (dateFrom != null && dateTo != null && dateFrom.isAfter(dateTo)) {
            throw new IllegalArgumentException("dateFrom은 dateTo보다 이전이어야 합니다.");
        }

        return reservationRepository.findAllByMemberIdAndThemeIdAndDateBetween(memberId, themeId, dateFrom, dateTo)
                .stream()
                .map(ReservationResponse::new)
                .toList();
    }

    public List<UserReservationsResponse> getUserReservations(final Long memberId) {
        findMemberById(memberId);

        return reservationRepository.findReservationsWithRankByMemberId(memberId).stream()
                .map(UserReservationsResponse::new)
                .toList();
    }

    @Transactional
    public void deleteReservationByUser(final Long reservationId, final Long memberId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalStateException("이미 삭제되어 있는 리소스입니다."));

        if (!Objects.equals(reservation.getMember().getId(), memberId)) {
            throw new IllegalStateException("다른 사람의 예약을 삭제할 수 없습니다.");
        }

        if (reservation.getStatus().equals(ReservationStatus.RESERVED)) {
            throw new IllegalStateException("이미 예약된 상태 내역은 삭제할 수 없습니다.");
        }

        reservationRepository.delete(reservation);
        acceptStatusByFirstWaiting(reservation);
    }

    @Transactional
    public void deleteReservationByAdmin(final Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalStateException("이미 삭제되어 있는 리소스입니다."));

        reservationRepository.deleteById(reservationId);
        acceptStatusByFirstWaiting(reservation);
    }

    private ReservationTime getReservationTime(Long timeId) {
        return reservationTimeRepository.findById(timeId)
                .orElseThrow(() -> new NoSuchElementException("예약 시간 정보를 찾을 수 없습니다."));
    }

    private Theme getTheme(final Long themeId) {
        return themeRepository.findById(themeId)
                .orElseThrow(() -> new NoSuchElementException("테마 정보를 찾을 수 없습니다."));
    }

    private void acceptStatusByFirstWaiting(final Reservation reservation) {
        reservationRepository.findFirstWaitingReservation(
                reservation.getDate(),
                reservation.getReservationTime(),
                reservation.getTheme()).acceptStatus();
    }

    private void validateReservationDateTime(LocalDate reservationDate, ReservationTime reservationTime) {
        final LocalDateTime reservationDateTime = LocalDateTime.of(reservationDate, reservationTime.getStartAt());

        validateIsPast(reservationDateTime);
        validateIsDuplicate(reservationDate, reservationTime);
    }

    private static void validateIsPast(LocalDateTime reservationDateTime) {
        if (reservationDateTime.isBefore(LocalDateTime.now())) {
            throw new DateTimeException("지난 일시에 대한 예약 생성은 불가능합니다.");
        }
    }

    private void validateIsDuplicate(final LocalDate reservationDate, final ReservationTime reservationTime) {
        if (reservationRepository.existsByDateAndReservationTimeStartAt(reservationDate,
                reservationTime.getStartAt())) {
            throw new IllegalStateException("중복된 일시의 예약은 불가능합니다.");
        }
    }

    private Member findMemberById(final Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new NoSuchElementException("유저 정보를 찾을 수 없습니다."));
    }

    public List<AdminWaitingReservationResponse> getWaitingReservation() {
        return reservationRepository.findReservationsWithRankOfWaitingStatus().stream()
                .map(AdminWaitingReservationResponse::new)
                .toList();
    }

    @Transactional
    public void acceptWaitingReservation(final Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(NoSuchElementException::new);

        reservation.acceptStatus();
    }
}
