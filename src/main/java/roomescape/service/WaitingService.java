package roomescape.service;

import org.springframework.stereotype.Service;
import roomescape.exception.BadRequestException;
import roomescape.exception.DuplicatedException;
import roomescape.exception.NotFoundException;
import roomescape.model.Reservation;
import roomescape.model.ReservationTime;
import roomescape.model.Waiting;
import roomescape.model.WaitingWithRank;
import roomescape.model.member.LoginMember;
import roomescape.model.member.Member;
import roomescape.model.theme.Theme;
import roomescape.repository.*;
import roomescape.service.dto.ReservationDto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
public class WaitingService { // TODO: 본인이 대기한 예약은 대기할 수 없도록

    private final WaitingRepository waitingRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final MemberRepository memberRepository;
    private final ThemeRepository themeRepository;
    private final ReservationRepository reservationRepository;

    public WaitingService(WaitingRepository waitingRepository,
                          ReservationTimeRepository reservationTimeRepository,
                          MemberRepository memberRepository,
                          ThemeRepository themeRepository,
                          ReservationRepository reservationRepository) {
        this.waitingRepository = waitingRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.memberRepository = memberRepository;
        this.themeRepository = themeRepository;
        this.reservationRepository = reservationRepository;
    }

    public List<Waiting> findAllWaiting() {
        return waitingRepository.findAll();
    }

    public Waiting saveWaiting(ReservationDto waitingDto) {
        ReservationTime time = findReservationTime(waitingDto);
        Theme theme = findTheme(waitingDto);
        Member member = findMember(waitingDto);
        Reservation reservation = findReservation(waitingDto);

        LocalDate date = waitingDto.getDate();
        validateIsReservationOwner(waitingDto.getMemberId(), reservation.getMember().getId());
        validateIsFuture(date, time.getStartAt());
        validateDuplication(date, time.getId(), waitingDto.getThemeId(), waitingDto.getMemberId());

        Waiting waiting = Waiting.of(waitingDto, time, theme, member);
        return waitingRepository.save(waiting);
    }

    private Reservation findReservation(ReservationDto waitingDto) {
        LocalDate date = waitingDto.getDate();
        long timeId = waitingDto.getTimeId();
        long themeId = waitingDto.getThemeId();
        Optional<Reservation> reservation = reservationRepository.findByDateAndTimeIdAndThemeId(date, timeId, themeId);
        return reservation.orElseThrow(() -> new BadRequestException("[ERROR] 존재하지 않는 데이터입니다."));
    }

    private void validateDuplication(LocalDate date, long timeId, long themeId, long memberId) {
        boolean isExist = waitingRepository.existsByDateAndTimeIdAndThemeIdAndMemberId(date, timeId, themeId, memberId);
        if (isExist) {
            throw new DuplicatedException("[ERROR] 중복된 예약 대기는 추가할 수 없습니다.");
        }
    }

    private void validateIsReservationOwner(long waitingMemberId, long reservationMemberId) {
        if (waitingMemberId == reservationMemberId) {
            throw new BadRequestException("[ERROR] 본인의 예약에는 대기할 수 없습니다.");
        }
    }

    private void validateIsFuture(LocalDate date, LocalTime time) {
        LocalDateTime timeToBook = LocalDateTime.of(date, time).truncatedTo(ChronoUnit.SECONDS);
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        if (timeToBook.isBefore(now)) {
            throw new BadRequestException("[ERROR] 현재 이전 예약은 대기할 수 없습니다.");
        }
    }

    private ReservationTime findReservationTime(ReservationDto reservationDto) {
        long timeId = reservationDto.getTimeId();
        Optional<ReservationTime> time = reservationTimeRepository.findById(timeId);
        return time.orElseThrow(() -> new BadRequestException("[ERROR] 존재하지 않는 데이터입니다."));
    }

    private Theme findTheme(ReservationDto reservationDto) {
        long themeId = reservationDto.getThemeId();
        Optional<Theme> theme = themeRepository.findById(themeId);
        return theme.orElseThrow(() -> new BadRequestException("[ERROR] 존재하지 않는 데이터입니다."));
    }

    private Member findMember(ReservationDto reservationDto) {
        long memberId = reservationDto.getMemberId();
        Optional<Member> member = memberRepository.findById(memberId);
        return member.orElseThrow(() -> new BadRequestException("[ERROR] 존재하지 않는 데이터입니다."));
    }

    public List<WaitingWithRank> findWaitingByMember(LoginMember member) {
        return waitingRepository.findWaitingWithRankByMemberId(member.getId());
    }

    public void deleteWaitingOfMember(long id) {
        // TODO: 본인의 예약 대기가 맞는지 검증 or deleteByIdAndMemberId
        validateExistence(id);
        waitingRepository.deleteById(id);
    }

    public void deleteWaiting(long id) {
        validateExistence(id);
        waitingRepository.deleteById(id);
    }

    private void validateExistence(long id) {
        boolean isNotExist = !waitingRepository.existsById(id);
        if (isNotExist) {
            throw new NotFoundException("[ERROR] 존재하지 않는 예약 대기입니다.");
        }
    }
}
