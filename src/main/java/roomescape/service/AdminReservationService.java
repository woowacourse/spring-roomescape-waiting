package roomescape.service;

import static roomescape.domain.reservation.ReservationStatus.RESERVED;
import static roomescape.domain.reservation.ReservationStatus.STANDBY;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.controller.dto.CreateReservationResponse;
import roomescape.controller.dto.FindReservationResponse;
import roomescape.controller.dto.FindReservationStandbyResponse;
import roomescape.domain.member.Member;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.theme.Theme;
import roomescape.global.exception.RoomescapeException;
import roomescape.repository.MemberRepository;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;

@Service
public class AdminReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;

    public AdminReservationService(
        ReservationRepository reservationRepository,
        ReservationTimeRepository reservationTimeRepository,
        ThemeRepository themeRepository,
        MemberRepository memberRepository) {

        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.memberRepository = memberRepository;
    }

    @Transactional
    public CreateReservationResponse reserve(Long memberId, LocalDate date, Long timeId, Long themeId) {
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new RoomescapeException("입력한 사용자 ID에 해당하는 데이터가 존재하지 않습니다."));
        ReservationTime time = reservationTimeRepository.findById(timeId)
            .orElseThrow(() -> new RoomescapeException("입력한 시간 ID에 해당하는 데이터가 존재하지 않습니다."));
        Theme theme = themeRepository.findById(themeId)
            .orElseThrow(() -> new RoomescapeException("입력한 테마 ID에 해당하는 데이터가 존재하지 않습니다."));
        LocalDateTime createdAt = LocalDateTime.now();

        Reservation reservation = new Reservation(member, date, createdAt, time, theme, RESERVED);
        validateDuplication(date, timeId, themeId);
        validatePastReservation(date, time);

        return CreateReservationResponse.from(reservationRepository.save(reservation));
    }

    private void validateDuplication(LocalDate date, Long timeId, Long themeId) {
        if (reservationRepository.existsByDateAndTimeIdAndThemeId(date, timeId, themeId)) {
            throw new RoomescapeException("해당 시간에 예약이 이미 존재합니다.");
        }
    }

    private void validatePastReservation(LocalDate date, ReservationTime time) {
        if (date.isBefore(LocalDate.now())) {
            throw new RoomescapeException("과거 예약을 추가할 수 없습니다.");
        }
        if (date.isEqual(LocalDate.now()) && time.isBeforeNow()) {
            throw new RoomescapeException("과거 예약을 추가할 수 없습니다.");
        }
    }

    @Transactional
    public void deleteById(Long id) {
        Reservation reservation = reservationRepository.findByIdAndStatus(id, RESERVED)
            .orElseThrow(() -> new RoomescapeException("예약이 존재하지 않아 삭제할 수 없습니다."));

        reservationRepository.deleteById(reservation.getId());
        approveNextWaiting(reservation);
    }

    @Transactional
    public void deleteStandby(Long id, Member member) {
        Reservation reservation = reservationRepository.findByIdAndStatus(id, STANDBY)
            .orElseThrow(() -> new RoomescapeException("예약대기가 존재하지 않아 삭제할 수 없습니다."));

        if (member.isNotAdmin() && reservation.isNotReservedBy(member)) {
            throw new RoomescapeException("자신의 예약만 삭제할 수 있습니다.");
        }

        reservationRepository.deleteById(reservation.getId());
        approveNextWaiting(reservation);
    }

    private void approveNextWaiting(Reservation reservation) {
        reservationRepository.findFirstByDateAndTimeIdAndThemeIdOrderByCreatedAtAsc(
            reservation.getDate(),
            reservation.getTime().getId(),
            reservation.getTheme().getId()
        ).ifPresent(Reservation::reserve);
    }

    @Transactional(readOnly = true)
    public List<FindReservationResponse> findAllReserved() {
        List<Reservation> reservations = reservationRepository.findAllByStatusOrderByDateAscTimeStartAtAsc(RESERVED);
        return reservations.stream()
            .map(FindReservationResponse::from)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<FindReservationStandbyResponse> findAllStandby() {
        List<Reservation> reservations = reservationRepository.findAllByStatusOrderByDateAscTimeStartAtAsc(STANDBY);
        return reservations.stream()
            .map(FindReservationStandbyResponse::from)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<FindReservationResponse> findAllByFilter(
        Long themeId, Long memberId, LocalDate dateFrom, LocalDate dateTo) {

        if (dateFrom.isAfter(dateTo)) {
            throw new RoomescapeException("날짜 조회 범위가 올바르지 않습니다.");
        }
        List<Reservation> reservations =
            reservationRepository.findAllByThemeIdAndMemberIdAndDateIsBetweenOrderByDateAscTimeStartAtAsc(
                themeId, memberId, dateFrom, dateTo);
        return reservations.stream()
            .map(FindReservationResponse::from)
            .toList();
    }
}
