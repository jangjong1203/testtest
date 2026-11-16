package edu.handong.web11.controller;

import edu.handong.web11.domain.Attachment;
import edu.handong.web11.repository.AttachmentRepository;
import edu.handong.web11.service.FileStorageService;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/attachments")
public class AttachmentController {
    private final AttachmentRepository attachments;
    private final FileStorageService storage;
    public AttachmentController(AttachmentRepository attachments, FileStorageService storage) {
        this.attachments = attachments; this.storage = storage;
    }
    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> download(@PathVariable Long id) {
        //URL로 넘어온 id를 통해 DB에서 정보를 찾고, 없으면 에러를 발생시킴
        Attachment file = attachments.findById(id).orElseThrow(() ->
            new ResponseStatusException(HttpStatus.NOT_FOUND, "첨부파일이 없습니다."));
        //파일정보를 이용해 서버에서 파일을 가져옴
        Resource resource = storage.load(file);
        //브라우저가 파일을 다운로드하게 만듬
        String disposition = ContentDisposition.attachment()
            .filename(file.getOriginalFilename(), StandardCharsets.UTF_8)
            .build().toString();
        //다운로드 헤더, 타입, 파일의 데이터들을 반환해서 요청에 응답함
        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .header(HttpHeaders.CONTENT_DISPOSITION, disposition)
            .body(resource);
    }
    // STUDENT TODO: GET /attachments/{id}/view (image preview).

    @GetMapping("/{id}/view")
    public ResponseEntity<Resource> view(@PathVariable Long id){
        //URL로 넘어온 id를 통해 DB에서 정보를 찾고, 없으면 에러를 발생시킴
        Attachment file = attachments.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "첨부파일이 없습니다."));
        //파일 타입을 검사해서 image/로 시작하는 파일 형식이 아니면 미리보기를 사용할수없도록 막습니다.
        String contentType = file.getContentType();
        if (!contentType.startsWith("image/")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "이미지 파일만 미리보기가 가능합니다.");
        }
        // 파일의 실제 타입도 받아와서 img파일은 화면에 렌더링 시켜준다.
        Resource resource = storage.load(file);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .body(resource);
    }
}
