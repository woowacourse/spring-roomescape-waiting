package roomescape.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.member.Member;
import roomescape.domain.reservation.ReservationStatus;
import roomescape.domain.reservation.ReservationWaitingRank;
import roomescape.domain.reservation.ReservationWaitingTicket;
import roomescape.dto.auth.LoginInfo;
import roomescape.dto.reservation.MyReservationResponseDto;
import roomescape.dto.reservation.ReservationResponseDto;
import roomescape.exception.DuplicateContentException;
import roomescape.exception.NotFoundException;
import roomescape.repository.JpaMemberRepository;
import roomescape.repository.JpaReservationRepository;
import roomescape.repository.JpaReservationTimeRepository;
import roomescape.repository.JpaReservationWaitingTicketRepository;
import roomescape.repository.JpaThemeRepository;
import roomescape.service.dto.ReservationCreateDto;

@Service
@Transactional
public class ReservationService {

    private final JpaReservationRepository reservationRepository;
    private final JpaReservationTimeRepository reservationTimeRepository;
    private final JpaThemeRepository themeRepository;
    private final JpaMemberRepository memberRepository;
    private final JpaReservationWaitingTicketRepository waitingTicketRepository;

    public ReservationService(final JpaReservationRepository reservationRepository,
                              final JpaReservationTimeRepository reservationTimeRepository,
                              final JpaThemeRepository themeRepository,
                              final JpaMemberRepository memberRepository,
                              JpaReservationWaitingTicketRepository waitingTicketRepository
    ) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.memberRepository = memberRepository;
        this.waitingTicketRepository = waitingTicketRepository;
    }

    public ReservationResponseDto createReservation(ReservationCreateDto dto) {
        ReservationTime reservationTime = reservationTimeRepository.findById(dto.timeId())
                .orElseThrow(() -> new NotFoundException("[ERROR] 예약 시간을 찾을 수 없습니다. id : " + dto.timeId()));

        validateDuplicate(dto.date(), dto.timeId(), dto.themeId());
        Reservation.validateReservableTime(dto.date(), reservationTime.getStartAt());

        Theme theme = themeRepository.findById(dto.themeId())
                .orElseThrow(() -> new NotFoundException("[ERROR] 테마를 찾을 수 없습니다. id : " + dto.themeId()));

        Member member = memberRepository.findById(dto.memberId())
                .orElseThrow(() -> new NotFoundException("[ERROR] 유저를 찾을 수 없습니다. id : " + dto.memberId()));

        Reservation requestReservation = Reservation.createWithoutId(member, dto.date(), reservationTime, theme);
        Reservation newReservation = reservationRepository.save(requestReservation);

        return ReservationResponseDto.of(newReservation);
    }

    private void validateDuplicate(LocalDate date, long timeId, long themeId) {
        List<Reservation> reservations = reservationRepository.findReservationsByDateAndTimeIdAndThemeId(date, timeId,
                themeId);
        if (!reservations.isEmpty()) {
            throw new DuplicateContentException("[ERROR] 이미 예약이 존재합니다. 예약 대기 기능을 사용해주세요.");
        }
    }

    @Transactional(readOnly = true)
    public List<ReservationResponseDto> findAllReservationResponses() {
        List<Reservation> allReservations = reservationRepository.findAll();

        return allReservations.stream()
                .map(ReservationResponseDto::of)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReservationResponseDto> findReservationBetween(long themeId, long memberId, LocalDate from,
                                                               LocalDate to) {
        List<Reservation> reservationsByPeriodAndMemberAndTheme = reservationRepository.findReservationsByDateBetweenAndThemeIdAndMemberId(
                from, to, themeId, memberId);
        return reservationsByPeriodAndMemberAndTheme.stream()
                .map(ReservationResponseDto::of)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<MyReservationResponseDto> findMyReservations(LoginInfo loginInfo) {
        List<Reservation> reservations = reservationRepository.findReservationsByMemberId(loginInfo.id());
        return reservations.stream().map(reservation -> {
            if (reservation.isReservationWaiting()) {
                ReservationWaitingTicket reservationWaitingTicket = waitingTicketRepository.findByReservationId(
                        reservation.getId());
                ReservationWaitingRank rank = waitingTicketRepository.countReservationWaitingsByThemeIdAndDateAndTimeIdAndCreatedAt(
                        reservation.getTheme().getId(),
                        reservation.getDate(),
                        reservation.getTime().getId(),
                        reservationWaitingTicket.getCreatedAt()
                );
                return new MyReservationResponseDto(
                        reservation, rank
                );
            }
            return new MyReservationResponseDto(reservation);
        }).toList();
    }

    @Transactional(readOnly = true)
    public List<ReservationResponseDto> findAllReservedReservations() {
        return reservationRepository.findReservationsByStatus(ReservationStatus.RESERVED).stream()
                .map(ReservationResponseDto::of)
                .toList();
    }

    public void deleteReservation(Long id) {
        Optional<Reservation> reservation = reservationRepository.findById(id);
        if (reservation.isEmpty()) {
            throw new NotFoundException("[ERROR] 등록된 예약번호만 삭제할 수 있습니다. 입력된 번호는 " + id + "입니다.");
        }
        reservationRepository.deleteById(id);

        updateReservationWaitingToReservation(reservation.get());
    }

    private void updateReservationWaitingToReservation(Reservation deletedReservation) {
        List<Reservation> reservationWaitings = reservationRepository.findReservationsByDateAndTimeIdAndThemeId(
                deletedReservation.getDate(),
                deletedReservation.getTime().getId(),
                deletedReservation.getTheme().getId()
        );

        if (reservationWaitings.isEmpty()) {
            return;
        }

        //todo 리팩토링
        LocalDateTime earliestCreatedAt = LocalDateTime.MAX;
        Long earliestReservationWaitingId = null;
        for (Reservation waiting : reservationWaitings) {
            ReservationWaitingTicket reservationWaitingTicket = waitingTicketRepository.findByReservationId(
                    waiting.getId());
            if (reservationWaitingTicket.getCreatedAt().isBefore(earliestCreatedAt)) {
                earliestCreatedAt = reservationWaitingTicket.getCreatedAt();
                earliestReservationWaitingId = reservationWaitingTicket.getId();
            }
        }

        ReservationWaitingTicket earliestReservationWaiting = waitingTicketRepository.findById(earliestReservationWaitingId).get();
        waitingTicketRepository.deleteById(earliestReservationWaitingId);
        Reservation updatingReservation = reservationRepository.findById(
                earliestReservationWaiting.getReservation().getId()).get();
        updatingReservation.setStatus(ReservationStatus.RESERVED);
    }
}
