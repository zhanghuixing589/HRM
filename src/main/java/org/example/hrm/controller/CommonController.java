package org.example.hrm.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping("/common")
@CrossOrigin
public class CommonController {

  @Value("${file.upload.path:/tmp/upload/}")
  private String uploadPath;

  @Value("${file.upload.url:http://localhost:8080/profile/upload/}")
  private String uploadUrl;

  @PostMapping("/upload")
  public Map<String, Object> uploadFile(@RequestParam("file") MultipartFile file) {
    try {
      String originalFilename = file.getOriginalFilename();
      String ext = originalFilename.substring(originalFilename.lastIndexOf("."));
      String fileName = UUID.randomUUID() + ext;

      File dir = new File(uploadPath);
      if (!dir.exists())
        dir.mkdirs();

      File dest = new File(dir, fileName);
      file.transferTo(dest);

      String fullUrl = uploadUrl + fileName;

      return Map.of("code", 200, "data", fullUrl, "msg", "上传成功");
    } catch (IOException e) {
      return Map.of("code", 500, "msg", "上传失败");
    }
  }
}