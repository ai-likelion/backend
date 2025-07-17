package com.likelion.ai_teacher_a.domain.userJr.service;

import com.likelion.ai_teacher_a.domain.image.entity.Image;
import com.likelion.ai_teacher_a.domain.image.repository.ImageRepository;
import com.likelion.ai_teacher_a.domain.user.entity.User;
import com.likelion.ai_teacher_a.domain.user.repository.UserRepository;
import com.likelion.ai_teacher_a.domain.userJr.dto.UserJrRequestDto;
import com.likelion.ai_teacher_a.domain.userJr.dto.UserJrResponseDto;
import com.likelion.ai_teacher_a.domain.userJr.dto.UserJrUpdateRequestDto;
import com.likelion.ai_teacher_a.domain.userJr.entity.UserJr;
import com.likelion.ai_teacher_a.domain.userJr.repository.UserJrRepository;
import com.likelion.ai_teacher_a.global.exception.CustomException;
import com.likelion.ai_teacher_a.global.exception.ErrorCode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserJrService {

    private final UserJrRepository userJrRepository;
    private final UserRepository userRepository;
    private final ImageRepository imageRepository;

    public UserJrResponseDto create(UserJrRequestDto dto) {
        User parent = userRepository.findById(dto.getParentId())
                .orElseThrow(() -> new RuntimeException("부모 사용자를 찾을 수 없습니다."));

        // ✅ 중복 자녀 이름 방지 (같은 부모 기준)
        boolean exists = userJrRepository.existsByParentIdAndName(dto.getParentId(), dto.getName());
        if (exists) {
            throw new RuntimeException("이미 같은 이름의 자녀가 등록되어 있습니다.");
        }

        // ✅ 학년 범위 유효성 검사 (1~9: 초1~중3)
        if (!isValidGrade(dto.getSchoolGrade())) {
            throw new IllegalArgumentException("학년은 초등학교 1학년부터 중학교 3학년(1~9)까지만 가능합니다.");
        }

        UserJr userJr = UserJr.builder()
                .parent(parent)
                .name(dto.getName())
                .schoolGrade(dto.getSchoolGrade())
                .build();

        return UserJrResponseDto.from(userJrRepository.save(userJr));
    }

    public List<UserJrResponseDto> findByParent(Long parentId) {
        return userJrRepository.findByParentId(parentId).stream()
                .map(UserJrResponseDto::from)
                .collect(Collectors.toList());
    }

    public UserJrResponseDto findById(Long userJrId) {
        return UserJrResponseDto.from(
                userJrRepository.findById(userJrId)
                        .orElseThrow(() -> new RuntimeException("자녀 정보를 찾을 수 없습니다."))
        );
    }

    public void setProfileImage(Long userJrId, Long imageId) {
        UserJr userJr = userJrRepository.findById(userJrId)
                .orElseThrow(() -> new RuntimeException("UserJr not found"));

        Image image = imageRepository.findById(imageId)
                .orElseThrow(() -> new RuntimeException("Image not found"));

        userJr.setProfileImage(image);
        userJrRepository.save(userJr);
    }

    public void delete(Long userJrId) {
        if (!userJrRepository.existsById(userJrId)) {
            throw new RuntimeException("삭제할 자녀 정보가 존재하지 않습니다.");
        }
        userJrRepository.deleteById(userJrId);
    }

    private boolean isValidGrade(int grade) {
        return grade >= 1 && grade <= 9;
    }

    @Transactional
    public void updateUserJr(Long userJrId, UserJrUpdateRequestDto dto) {
        UserJr userJr = userJrRepository.findById(userJrId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_JR_NOT_FOUND));

        if (dto.getName() != null) {
            userJr.setName(dto.getName());
        }

        if (dto.getNickname() != null) {
            userJr.setNickname(dto.getNickname());
        }

        if (dto.getSchoolGrade() != null) {
            userJr.setSchoolGrade(dto.getSchoolGrade());
        }

        // 저장은 @Transactional 로 자동 flush 또는 명시적으로 save 가능
    }
}
