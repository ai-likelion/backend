package com.likelion.ai_teacher_a.domain.userJr.service;

import com.likelion.ai_teacher_a.domain.image.entity.Image;
import com.likelion.ai_teacher_a.domain.image.entity.ImageType;
import com.likelion.ai_teacher_a.domain.image.repository.ImageRepository;
import com.likelion.ai_teacher_a.domain.image.service.ImageService;
import com.likelion.ai_teacher_a.domain.image.service.S3Uploader;
import com.likelion.ai_teacher_a.domain.logsolve.entity.LogSolve;
import com.likelion.ai_teacher_a.domain.logsolve.repository.LogSolveRepository;
import com.likelion.ai_teacher_a.domain.user.entity.User;
import com.likelion.ai_teacher_a.domain.user.repository.UserRepository;
import com.likelion.ai_teacher_a.domain.userJr.dto.UserJrRequestDto;
import com.likelion.ai_teacher_a.domain.userJr.dto.UserJrResponseDto;
import com.likelion.ai_teacher_a.domain.userJr.dto.UserJrUpdateRequestDto;
import com.likelion.ai_teacher_a.domain.userJr.entity.UserJr;
import com.likelion.ai_teacher_a.domain.userJr.repository.UserJrRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserJrService {

    private final UserJrRepository userJrRepository;
    private final UserRepository userRepository;
    private final LogSolveRepository logSolveRepository;
    private final ImageRepository imageRepository;
    private final ImageService imageService;
    private final S3Uploader s3Uploader;


    @Transactional
    public UserJrResponseDto create(UserJrRequestDto dto, MultipartFile imageFile, Long userId) {
        User user = userRepository.getReferenceById(userId);


        boolean exists = userJrRepository.existsByUserIdAndNickname(userId, dto.getNickname());


        if (exists) {
            throw new RuntimeException("이미 같은 이름의 자녀가 등록되어 있습니다.");
        }

        if (!isValidGrade(dto.getSchoolGrade())) {
            throw new IllegalArgumentException("학년은 초등학교 1학년부터 6학년까지만 가능합니다.");
        }

        Image image = null;
        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                image = imageService.uploadToS3AndSaveWithEntity(imageFile, ImageType.PROFILE, userId).getImage();
            } catch (IOException e) {
                throw new RuntimeException("이미지 업로드 중 오류가 발생했습니다.", e);
            }
        }


        UserJr userJr = UserJr.builder()
                .user(user)
                .nickname(dto.getNickname())
                .schoolGrade(dto.getSchoolGrade())
                .image(image)
                .build();


        return UserJrResponseDto.from(userJrRepository.save(userJr));
    }


    public List<UserJrResponseDto> findByParent(Long userId) {
        return userJrRepository.findByUserIdFetchJoin(userId).stream()
                .map(UserJrResponseDto::from)
                .collect(Collectors.toList());
    }

    public UserJrResponseDto findById(Long userJrId, Long userId) {
        UserJr userJr = userJrRepository.findByIdWithUserAndImage(userJrId)
                .filter(jr -> jr.getUser().getId().equals(userId))
                .orElseThrow(() -> new RuntimeException("자녀 정보를 찾을 수 없거나 권한이 없습니다."));

        return UserJrResponseDto.from(userJr);
    }


    @Transactional
    public void delete(Long userJrId, Long userId) {

        UserJr userJr = userJrRepository.findByIdWithUserAndImage(userJrId)
                .filter(jr -> jr.getUser().getId().equals(userId))
                .orElseThrow(() -> new RuntimeException("삭제할 자녀 정보가 존재하지 않거나 권한이 없습니다."));


        List<LogSolve> logs = logSolveRepository.findAllByUserJrWithImage(userJr);


        logs.parallelStream()
                .map(LogSolve::getImage)
                .filter(img -> img != null && img.getUrl() != null)
                .forEach(img -> s3Uploader.delete(img.getUrl()));


        logSolveRepository.deleteAll(logs);


        Image profileImage = userJr.getImage();
        if (profileImage != null && profileImage.getUrl() != null) {
            s3Uploader.delete(profileImage.getUrl());
            imageRepository.delete(profileImage);
        }


        userJrRepository.delete(userJr);
    }


    private boolean isValidGrade(int grade) {
        return grade >= 1 && grade <= 6;
    }

    @Transactional
    public void updateUserJr(Long userJrId, UserJrUpdateRequestDto dto, Long userId, MultipartFile imageFile) throws IOException {
        UserJr userJr = userJrRepository.findByIdWithUser(userJrId)
                .orElseThrow(() -> new RuntimeException("자녀 정보를 찾을 수 없습니다."));

        if (!userJr.getUser().getId().equals(userId)) {
            throw new RuntimeException("해당 자녀에 접근할 권한이 없습니다.");
        }

        userJr.setNickname(dto.getNickname());
        userJr.setSchoolGrade(dto.getSchoolGrade());

        if (imageFile != null && !imageFile.isEmpty()) {
            Image oldImage = userJr.getImage();
            if (oldImage != null && oldImage.getUrl() != null) {
                s3Uploader.delete(oldImage.getUrl());
                imageRepository.delete(oldImage);
            }

            String newUrl = s3Uploader.upload(imageFile);
            Image newImage = Image.builder()
                    .fileName(imageFile.getOriginalFilename())
                    .fileSize((int) imageFile.getSize())
                    .type(ImageType.PROFILE)
                    .url(newUrl)
                    .user(userJr.getUser())
                    .build();

            imageRepository.save(newImage);
            userJr.setImage(newImage);
        }

        userJrRepository.save(userJr);
    }


}
