package hgcq.photobook.controller;

import hgcq.photobook.domain.Event;
import hgcq.photobook.domain.Member;
import hgcq.photobook.domain.Photo;
import hgcq.photobook.dto.MemberDTO;
import hgcq.photobook.dto.PhotoDTO;
import hgcq.photobook.service.EventService;
import hgcq.photobook.service.MemberService;
import hgcq.photobook.service.PhotoService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class PhotoController {

    private static final Logger log = LoggerFactory.getLogger(PhotoController.class);

    private final PhotoService photoService;
    private final EventService eventService;
    private final MemberService memberService;

    private String directoryPath = "D:" + File.separator
            + "app" + File.separator
            + "images" + File.separator;

    @PostMapping("/photo/upload")
    public ResponseEntity<?> uploadPhoto(@ModelAttribute PhotoDTO photoDTO
            , HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            MemberDTO loginMember = (MemberDTO) session.getAttribute("member");

            if (loginMember != null) {
                Member member = memberService.findOne(loginMember.getEmail());

                if (member != null) {
                    MultipartFile image = photoDTO.getImage();

                    if (image != null && !image.isEmpty()) {
                        try {
                            Event findEvent = eventService.searchEventByDate(LocalDate.parse(photoDTO.getDate()), member);
                            String newPath = directoryPath + findEvent.getId() + File.separator;
                            File directory = new File(newPath);

                            if (!directory.exists()) {
                                directory.mkdirs();
                            }

                            String imageName = image.getOriginalFilename();
                            Path path = Paths.get(newPath + imageName);

                            image.transferTo(path);

                            String imagePath = "/images/" + findEvent.getId() + "/" + imageName;

                            Photo photo = new Photo(imageName, imagePath, findEvent);
                            photoService.uploadPhoto(photo, member);

                            return ResponseEntity.ok("사진 업로드 성공");
                        } catch (Exception e) {
                            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("사진 업로드 중 에러 발생");
                        }
                    }
                }
            }
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("사진 업로드 실패");
    }

    @PostMapping("/photo/delete")
    public ResponseEntity<?> deletePhoto(@RequestBody PhotoDTO photoDTO, HttpServletRequest request) {
        HttpSession session = request.getSession(false);

        if (session != null) {
            MemberDTO loginMember = (MemberDTO) session.getAttribute("member");

            if (loginMember != null) {
                Member member = memberService.findOne(loginMember.getEmail());

                if (member != null) {
                    boolean isDelete = photoService.deletePhoto(photoDTO.getPath(), member);

                    if (isDelete) {
                        try {
                            String newPath = "D:" + File.separator
                                    + "app" + File.separator + photoDTO.getPath();

                            Files.deleteIfExists(Path.of(newPath));

                            return ResponseEntity.ok("사진 삭제 성공");
                        } catch (Exception e) {
                            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("사진 삭제 중 에러 발생");
                        }
                    }
                }
            }
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("사진 삭제 실패");
    }

    @GetMapping("/photo/photos")
    public ResponseEntity<?> getPhotos(@RequestParam("date") String date, HttpServletRequest request) {
        HttpSession session = request.getSession(false);

        if (session != null) {
            MemberDTO loginMember = (MemberDTO) session.getAttribute("member");

            if (loginMember != null) {
                Member member = memberService.findOne(loginMember.getEmail());

                if (member != null) {
                    Event findEvent = eventService.searchEventByDate(LocalDate.parse(date), member);

                    if (findEvent != null) {
                        List<String> photos = photoService.photoList(findEvent, member);
                        return ResponseEntity.ok(photos);
                    }
                }
            }
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("사진 리스트 조회 실패");
    }

}
