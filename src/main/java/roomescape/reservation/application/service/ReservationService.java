package roomescape.reservation.application.service;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.member.application.repository.MemberRepository;
import roomescape.member.domain.Member;
import roomescape.reservation.application.repository.ReservationRepository;
import roomescape.reservation.application.repository.ReservationTimeRepository;
import roomescape.reservation.application.repository.ThemeRepository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.Theme;
import roomescape.reservation.presentation.dto.AdminReservationRequest;
import roomescape.reservation.presentation.dto.ReservationRequest;
import roomescape.reservation.presentation.dto.ReservationResponse;

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

        final Reservation reservation = new Reservation(
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

        Member member = memberRepository.findById(adminReservationRequest.getMemberId())
                .orElseThrow(() -> new NoSuchElementException("유저 정보를 찾을 수 없습니다."));

        final Reservation reservation = new Reservation(
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

    @Transactional
    public void deleteReservation(final Long id) {

        reservationRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("이미 삭제되어 있는 리소스입니다."));

        reservationRepository.deleteById(id);
    }

    private ReservationTime getReservationTime(Long timeId) {
        return reservationTimeRepository.findById(timeId)
                .orElseThrow(() -> new NoSuchElementException("예약 시간 정보를 찾을 수 없습니다."));
    }

    private Theme getTheme(final Long themeId) {
        return themeRepository.findById(themeId)
                .orElseThrow(() -> new NoSuchElementException("테마 정보를 찾을 수 없습니다."));
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
}
