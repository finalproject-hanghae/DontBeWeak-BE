package com.finalproject.dontbeweak.service;

import com.finalproject.dontbeweak.dto.pill.*;
import com.finalproject.dontbeweak.auth.UserDetailsImpl;
import com.finalproject.dontbeweak.exception.CustomException;
import com.finalproject.dontbeweak.exception.ErrorCode;
import com.finalproject.dontbeweak.model.User;
import com.finalproject.dontbeweak.repository.UserRepository;
import com.finalproject.dontbeweak.model.pill.Pill;
import com.finalproject.dontbeweak.model.pill.PillHistory;
import com.finalproject.dontbeweak.repository.pill.PillHistoryRepository;
import com.finalproject.dontbeweak.repository.pill.PillRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.finalproject.dontbeweak.exception.ErrorCode.PILL_DUPLICATION_CODE;

@Service
@RequiredArgsConstructor
public class PillService {
    private final PillRepository pillRepository;
    private final UserRepository userRepository;
    private final PillHistoryRepository pillHistoryRepository;

    private static final Integer GET_POINT = 10;

    //영양제 등록
    @Transactional
    public PillResponseDto registerPill(PillRequestDto pillRequestDto, UserDetailsImpl userDetails){
        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow(
                () -> new IllegalArgumentException("회원이 존재하지 않습니다.")
        );

        boolean isProductName = pillRepository.existsByUser_IdAndProductName(userDetails.getUser().getId(), pillRequestDto.getProductName());

        if(isProductName){
            throw new CustomException(PILL_DUPLICATION_CODE);
        }

        Pill pill = new Pill(user, pillRequestDto);
        pillRepository.save(pill);

        return new PillResponseDto(pill);
    }

    //영양제 조회
    public List<PillResponseDto> showPill(@PathVariable String username) {
        List<PillResponseDto> pillResponseDtoList = new ArrayList<>();
        List<Pill> pillList = pillRepository.findByUser_Username(username);

        for (Pill pill : pillList) {
            PillResponseDto pillResponseDto = new PillResponseDto(pill);
            pillResponseDtoList.add(pillResponseDto);
        }
        return pillResponseDtoList;
    }

    //영양제 복용 완료
    @Transactional
    public PillHistoryResponseDto donePill(PillHistoryRequestDto pillHistoryRequestDto, UserDetailsImpl userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow(
                () -> new IllegalArgumentException("회원이 존재하지 않습니다.")
        );

        String productName = pillHistoryRequestDto.getProductName();

        Pill pill = pillRepository.findByUser_IdAndProductName(user.getId(), productName);
        pill.donePill();
        pillRepository.save(pill);

        PillHistory pillHistory = new PillHistory(user, pill, pillHistoryRequestDto);
        pillHistoryRepository.save(pillHistory);

        int userPoint = user.getPoint();
        user.setPoint(userPoint + GET_POINT);

        return new PillHistoryResponseDto(pill, pillHistory);
    }

    //영양제 복용 여부 초기화
    @Transactional
    public Long update(Long id, boolean done){
        Pill pill = pillRepository.findById(id).orElseThrow(
                () -> new IllegalArgumentException("영양제가 존재하지 않습니다.")
        );

        pill.reset(done);
        return id;
    }

    //주간 영양제 복용 조회
    @Transactional
    public List<WeekPillHistoryResponseDto> getPillList(String username, String startDate, String endDate) {
        Optional<User> user = userRepository.findByUsername(username);
        Long userId = user.get().getId();

        LocalDateTime startDateTime = LocalDate.parse(startDate, DateTimeFormatter.ofPattern("yyyyMMdd")).atTime(0, 0, 0);
        LocalDateTime endDateTime = LocalDate.parse(endDate, DateTimeFormatter.ofPattern("yyyyMMdd")).atTime(23, 59, 59);

        List<PillHistory> pillHistoryList = pillHistoryRepository.findAllByUser_IdAndUsedAtBetween(userId, startDateTime, endDateTime);
        List<WeekPillHistoryResponseDto> pillHistoryResponseDtoList = new ArrayList<>();

        for (PillHistory pillHistory : pillHistoryList) {
            int dayOfWeekValue = pillHistory.getUsedAt().getDayOfWeek().getValue();

            WeekPillHistoryResponseDto weekPillDto = new WeekPillHistoryResponseDto(pillHistory, dayOfWeekValue);

            pillHistoryResponseDtoList.add(weekPillDto);
        }

        return pillHistoryResponseDtoList;
    }

}

