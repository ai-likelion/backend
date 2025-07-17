package com.likelion.ai_teacher_a.domain.userJr.service;

import com.likelion.ai_teacher_a.domain.image.entity.Image;
import com.likelion.ai_teacher_a.domain.image.repository.ImageRepository;
import com.likelion.ai_teacher_a.domain.user.entity.User;
import com.likelion.ai_teacher_a.domain.user.repository.UserRepository;
import com.likelion.ai_teacher_a.domain.userJr.dto.UserJrRequestDto;
import com.likelion.ai_teacher_a.domain.userJr.dto.UserJrResponseDto;
import com.likelion.ai_teacher_a.domain.userJr.entity.UserJr;
import com.likelion.ai_teacher_a.domain.userJr.repository.UserJrRepository;
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

        UserJr jr = new UserJr();
        jr.setParent(parent);
        jr.setName(dto.getName());
        jr.setSchoolGrade(dto.getSchoolGrade());

        return UserJrResponseDto.from(userJrRepository.save(jr));
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

    // 기존 자녀 생성, 조회, 삭제 등 생략...

    public void setProfileImage(Long userJrId, Long imageId) {
        UserJr userJr = userJrRepository.findById(userJrId)
                .orElseThrow(() -> new RuntimeException("UserJr not found"));

        Image image = imageRepository.findById(imageId)
                .orElseThrow(() -> new RuntimeException("Image not found"));

        userJr.setProfileImage(image);
        userJrRepository.save(userJr);
    }

    public void delete(Long userJrId) {
        userJrRepository.deleteById(userJrId);
    }
}

