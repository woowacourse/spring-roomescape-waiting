package roomescape.service;

import static roomescape.model.Reservation.createAcceptReservation;
import static roomescape.model.Reservation.createWaiting;
import static roomescape.model.ReservationStatus.WAITING;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import roomescape.controller.request.AdminReservationRequest;
import roomescape.controller.request.ReservationRequest;
import roomescape.exception.BadRequestException;
import roomescape.exception.DuplicatedException;
import roomescape.exception.NotFoundException;
import roomescape.model.Member;
import roomescape.model.Reservation;
import roomescape.model.ReservationTime;
import roomescape.model.Theme;
import roomescape.repository.DeletedReservationRepository;
import roomescape.repository.MemberRepository;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;
import roomescape.service.dto.MemberReservation;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;
    private final DeletedReservationRepository deletedReservationRepository;

    private final Logger logger = LoggerFactory.getLogger(ReservationService.class);

    public ReservationService(ReservationRepository reservationRepository,
                              ReservationTimeRepository reservationTimeRepository,
                              ThemeRepository themeRepository,
                              MemberRepository memberRepository,
                              DeletedReservationRepository deletedReservationRepository) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.memberRepository = memberRepository;
        this.deletedReservationRepository = deletedReservationRepository;
    }

    public List<Reservation> findAllReservations() {
        return reservationRepository.findAll();
    }

    public List<Reservation> filterReservation(Long themeId, Long memberId, LocalDate dateFrom, LocalDate dateTo) {
        Theme theme = findThemeById(themeId);
        Member member = findMemberById(memberId);
        return reservationRepository.findByThemeAndMemberAndDateBetween(theme, member, dateFrom, dateTo);
    }

    public Reservation addReservation(ReservationRequest request, Member member) {
        validateDuplicatedReservation(request.date(), request.timeId(), request.themeId());
        ReservationTime reservationTime = findReservationTime(request.date(), request.timeId());
        Theme theme = findThemeById(request.themeId());
        Reservation reservation = createAcceptReservation(request.date(), reservationTime, theme, member);
        return reservationRepository.save(reservation);
    }

    public Reservation addReservation(AdminReservationRequest request) {
        validateDuplicatedReservation(request.date(), request.timeId(), request.themeId());
        ReservationTime reservationTime = findReservationTime(request.date(), request.timeId());
        Theme theme = findThemeById(request.themeId());
        Member member = findMemberById(request.memberId());
        Reservation reservation = createAcceptReservation(request.date(), reservationTime, theme, member);
        return reservationRepository.save(reservation);
    }

    public Reservation addWaitingReservation(ReservationRequest request, Member member) {
        validateDuplicatedWaitingReservation(request.date(), request.timeId(), request.themeId(), member.getId());
        ReservationTime reservationTime = findReservationTime(request.date(), request.timeId());
        Theme theme = findThemeById(request.themeId());
        Reservation reservation = createWaiting(request.date(), reservationTime, theme, member);
        return reservationRepository.save(reservation);
    }


    public List<MemberReservation> findMemberReservations(Member member) {
        return reservationRepository.findMemberReservation(member.getId());
    }

    public List<Reservation> findWaitingReservations() {
        return reservationRepository.findAllReservationByStatus(WAITING);
    }

    @Transactional
    public void deleteReservation(long id) {
        validateExistReservation(id);
        Reservation reservation = findReservationById(id);
        if (reservation.isAcceptReservation()) {
            List<Reservation> waitingReservations = findWaitingReservation(reservation);
            confirmReservation(waitingReservations);
        }
        cancelReservation(reservation);
    }

    private void cancelReservation(Reservation reservation) {
        deletedReservationRepository.save(reservation.toDeletedReservation());
        reservationRepository.deleteById(reservation.getId());
    }

    private void validateReservationDateTimeBeforeNow(LocalDate date, LocalTime time) {
        LocalDateTime reservationDateTime = LocalDateTime.of(date, time).truncatedTo(ChronoUnit.SECONDS);
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        if (reservationDateTime.isBefore(now)) {
            logger.error("현재 시간 이전으로 예약할 수 없습니다 : 현재 시간={} 예약 시간={}", now, reservationDateTime);
            throw new BadRequestException(now);
        }
    }

    private void validateDuplicatedReservation(LocalDate date, Long timeId, Long themeId) {
        ReservationTime reservationTime = findReservationTimeById(timeId);
        Theme theme = findThemeById(themeId);
        long countReservation = reservationRepository.countByDateAndTimeAndTheme(date, reservationTime, theme);
        if (countReservation > 0) {
            logger.error("이미 예약이 존재합니다 : 예약 시간={}", LocalDateTime.of(date, reservationTime.getStartAt()));
            throw new DuplicatedException("예약");
        }
    }

    private void validateDuplicatedWaitingReservation(LocalDate date, Long timeId, Long themeId, Long memberId) {
        ReservationTime reservationTime = findReservationTimeById(timeId);
        Theme theme = findThemeById(themeId);
        Member member = findMemberById(memberId);
        long countReservation = reservationRepository
                .countByDateAndTimeAndThemeAndMember(date, reservationTime, theme, member);
        if (countReservation > 0) {
            logger.error("이미 예약을 했거나 예약 대기를 걸어놓았습니다 : 사용자 아이디={} 예약 시간={} 예약 테마 아이디 ={}",
                    member.getId(), LocalDateTime.of(date, reservationTime.getStartAt()), theme.getId());
            throw new DuplicatedException("예약");
        }
    }

    private Reservation findReservationById(long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("예약 시간이 존재하지 않습니다 : 예약 아이디={}", id);
                    return new NotFoundException("예약", id);
                });
    }

    private List<Reservation> findWaitingReservation(Reservation reservation) {
        return reservationRepository.findWaitingReservationsByReservation(
                reservation.getDate(),
                reservation.getTime().getId(),
                reservation.getTheme().getId());
    }

    private void confirmReservation(List<Reservation> waitingReservations) {
        waitingReservations.stream()
                .min(Comparator.comparing(Reservation::getCreatedAt))
                .ifPresent(Reservation::confirmReservation);
    }

    private ReservationTime findReservationTimeById(Long timeId) {
        return reservationTimeRepository.findById(timeId)
                .orElseThrow(() -> {
                    logger.error("예약 시간이 존재하지 않습니다 : 예약 시간 아이디={}", timeId);
                    return new NotFoundException("예약 시간", timeId);
                });
    }

    private ReservationTime findReservationTime(LocalDate date, long timeId) {
        ReservationTime reservationTime = findReservationTimeById(timeId);
        validateReservationDateTimeBeforeNow(date, reservationTime.getStartAt());
        return reservationTime;
    }

    private void validateExistReservation(long id) {
        long count = reservationRepository.countById(id);
        if (count <= 0) {
            logger.error("아이디에 해당하는 예약이 존재하지 않습니다 : reservationId={}", id);
            throw new NotFoundException("예약", id);
        }
    }

    private Theme findThemeById(Long themeId) {
        return themeRepository.findById(themeId)
                .orElseThrow(() -> {
                    logger.error("아이디에 해당하는 테마가 존재하지 않습니다 : themeId={}", themeId);
                    return new NotFoundException("테마", themeId);
                });
    }

    private Member findMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> {
                    logger.error("아이디에 해당하는 사용자가 존재하지 않습니다 : memberId={}", memberId);
                    return new NotFoundException("사용자", memberId);
                });
    }
}
