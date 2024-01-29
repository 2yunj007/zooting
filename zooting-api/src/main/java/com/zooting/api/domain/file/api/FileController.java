package com.zooting.api.domain.file.api;

import com.zooting.api.domain.file.application.FileService;
import com.zooting.api.domain.file.dto.request.FileReq;
import com.zooting.api.domain.file.dto.response.FileRes;
import com.zooting.api.domain.file.entity.File;
import com.zooting.api.global.common.BaseResponse;
import com.zooting.api.global.common.code.SuccessCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Log4j2
@RestController
@RequestMapping("api/file")
@RequiredArgsConstructor
@Tag(name="파일", description = "파일 관련 API")
public class FileController {
    private final FileService fileService;

    @Transactional
    @Operation(summary = "파일 업로드", description = "파일 업로드")
    @PostMapping("/upload")
    public ResponseEntity<BaseResponse<List<FileRes>>> uploadFile(List<MultipartFile> files) throws IOException {
        List<FileRes> fileResList = fileService.uploadFiles(files);
        return BaseResponse.success(
                SuccessCode.INSERT_SUCCESS,
                fileResList
        );
    }

    @Transactional
    @Operation(summary = "파일 삭제", description = "파일 삭제")
    @DeleteMapping("/delete")
    public ResponseEntity<BaseResponse<String>> deleteFile(@RequestBody FileReq fileReq) {
        fileService.removeFile(fileReq.fileName(), fileReq.fileDir());
        return BaseResponse.success(
                SuccessCode.DELETE_SUCCESS,
                "파일 삭제 성공"
        );
    }

    @Operation(summary = "파일 다운로드", description = "파일 다운로드")
    @GetMapping("/download/{fileName}")
    public ResponseEntity<byte[]> downloadFile(@PathVariable String fileName) throws IOException {
        var downloadFile = fileService.downloadFile(fileName);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        httpHeaders.setContentDispositionFormData("attachment", (String) downloadFile[1]);
        return new ResponseEntity<>((byte[]) downloadFile[0], httpHeaders, HttpStatus.OK);
    }
}
