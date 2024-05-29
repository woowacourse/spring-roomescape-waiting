package roomescape.service;

import org.springframework.stereotype.Service;
import roomescape.exception.BadRequestException;
import roomescape.exception.DuplicatedException;
import roomescape.exception.NotFoundException;
import roomescape.model.*;
import roomescape.model.member.LoginMember;
import roomescape.model.member.Member;
import roomescape.model.theme.Theme;
import roomescape.repository.*;
import roomescape.service.dto.WaitingDto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class WaitingService {

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

    public Waiting saveWaiting(WaitingDto waitingDto) {
        LocalDate date = waitingDto.getDate();
        ReservationTime time = findReservationTime(waitingDto);
        Theme theme = findTheme(waitingDto);
        Member member = findMember(waitingDto);
        Reservation reservation = findReservation(waitingDto, time, theme);

        validateIsFuture(date, time);
        validateDuplication(date, time, theme, member);
        validateIsReservationOwner(member, reservation.getMember());

        Waiting waiting = new Waiting(date, time, theme, member);
        return waitingRepository.save(waiting);
    }

    private ReservationTime findReservationTime(WaitingDto waitingDto) {
        long timeId = waitingDto.getTimeId();
        return reservationTimeRepository.findById(timeId)
                .orElseThrow(() -> new BadRequestException("[ERROR] 존재하지 않는 시간에 대한 예약 대기입니다."));
    }

    private Theme findTheme(WaitingDto waitingDto) {
        long themeId = waitingDto.getThemeId();
        return themeRepository.findById(themeId)
                .orElseThrow(() -> new BadRequestException("[ERROR] 존재하지 않는 테마에 대한 예약 대기입니다."));
    }

    private Member findMember(WaitingDto waitingDto) {
        long memberId = waitingDto.getMemberId();
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new BadRequestException("[ERROR] 존재하지 않는 사용자에 대한 예약 대기입니다."));
    }

    private Reservation findReservation(WaitingDto waitingDto, ReservationTime time, Theme theme) {
        LocalDate date = waitingDto.getDate();
        ReservationInfo reservationInfo = new ReservationInfo(date, time, theme);
        return reservationRepository.findByReservationInfo(reservationInfo)
                .orElseThrow(() -> new BadRequestException("[ERROR] 존재하지 않는 예약에 대한 예약 대기입니다."));
    }

    public List<WaitingWithRank> findWaitingByMember(LoginMember member) {
        return waitingRepository.findWaitingWithRankByMemberId(member.getId());
    }

    public void deleteOwnWaiting(long id, LoginMember member) {
        validateExistence(id);
        validateIsOwner(id, member);
        waitingRepository.deleteById(id);
    }

    public void deleteWaiting(long id) {
        validateExistence(id);
        waitingRepository.deleteById(id);
    }

    private void validateIsFuture(LocalDate date, ReservationTime reservationTime) {
        LocalTime time = reservationTime.getStartAt();
        LocalDateTime timeToBook = LocalDateTime.of(date, time).truncatedTo(ChronoUnit.SECONDS);
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        if (timeToBook.isBefore(now)) {
            throw new BadRequestException("[ERROR] 현재 이전 예약은 대기할 수 없습니다.");
        }
    }

    private void validateDuplication(LocalDate date, ReservationTime time, Theme theme, Member member) {
        ReservationInfo reservationInfo = new ReservationInfo(date, time, theme);
        boolean isExist = waitingRepository.existsByReservationInfoAndMember(reservationInfo, member);
        if (isExist) {
            throw new DuplicatedException("[ERROR] 중복된 예약 대기는 추가할 수 없습니다.");
        }
    }

    private void validateIsReservationOwner(Member waitingMember, Member reservationMember) {
        boolean isReservationOwner = waitingMember.getId().equals(reservationMember.getId());
        if (isReservationOwner) {
            throw new BadRequestException("[ERROR] 본인의 예약에는 대기할 수 없습니다.");
        }
    }

    private void validateIsOwner(long id, LoginMember member) {
        boolean isNotOwner = !waitingRepository.existsByIdAndMemberId(id, member.getId());
        if (isNotOwner) {
            throw new BadRequestException("[ERROR] 해당 예약 대기의 소유자가 아닙니다.");
        }
    }

    private void validateExistence(long id) {
        boolean isNotExist = !waitingRepository.existsById(id);
        if (isNotExist) {
            throw new NotFoundException("[ERROR] 존재하지 않는 예약 대기입니다.");
        }
    }
}
