package com.example.demo.board.controller;

import com.example.demo.board.entity.Board;
import com.example.demo.board.repository.BoardRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/boards")
public class BoardController {

    private final BoardRepository boardRepository;

    @Value("${file.upload-dir}")
    private String uploadDir;

    public BoardController(BoardRepository boardRepository) {
        this.boardRepository = boardRepository;
    }

    // 게시글 생성 및 이미지 업로드
    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<Board> createBoard(
            @RequestPart("board") Board board,
            @RequestPart("image") MultipartFile image) throws IOException {

        // 이미지 파일 검증
        if (!image.isEmpty() && isImageFile(image)) {
            String fileName = UUID.randomUUID().toString() + "_" + image.getOriginalFilename();
            String filePath = uploadDir + File.separator + fileName;

            // 파일 저장
            File destinationFile = new File(filePath);
            image.transferTo(destinationFile);

            // 게시글 엔티티에 이미지 경로 설정
            board.setImagePath(filePath);
        } else if (!image.isEmpty()) {
            return ResponseEntity.badRequest().body(null); // 이미지가 아닌 파일일 경우
        }

        // 게시글 저장
        Board savedBoard = boardRepository.save(board);

        return ResponseEntity.ok(savedBoard);
    }

    // 이미지 파일 여부 검증
    private boolean isImageFile(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType != null && (contentType.startsWith("image/png") ||
                contentType.startsWith("image/jpeg") ||
                contentType.startsWith("image/jpg") ||
                contentType.startsWith("image/gif"));
    }

    // 게시글 전체 조회
    @GetMapping
    public List<Board> getAllBoards() {
        return boardRepository.findAll();
    }

    // 게시글 상세 조회
    @GetMapping("/{id}")
    public ResponseEntity<Board> getBoardById(@PathVariable Long id) {
        return boardRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }


    // 게시글 수정
    @PutMapping("/{id}")
    public ResponseEntity<Board> updateBoard(@PathVariable Long id, @RequestBody Board boardDetails) {
        return boardRepository.findById(id)
                .map(board -> {
                    board.setTitle(boardDetails.getTitle());
                    board.setContent(boardDetails.getContent());
                    Board updatedBoard = boardRepository.save(board);
                    return ResponseEntity.ok(updatedBoard);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // 게시글 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBoard(@PathVariable Long id) {
        if (boardRepository.existsById(id)) {
            boardRepository.deleteById(id);
            return ResponseEntity.noContent().build(); // 204 No Content
        } else {
            return ResponseEntity.notFound().build(); // 404 Not Found
        }
    }
}