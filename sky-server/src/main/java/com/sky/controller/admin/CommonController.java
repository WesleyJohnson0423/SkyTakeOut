package com.sky.controller.admin;

import com.sky.constant.MessageConstant;
import com.sky.result.Result;
import com.sky.utils.AliOssUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Api(tags = "公用接口")
@RestController
@Slf4j
@RequestMapping("/admin/common")
public class CommonController {

    @Autowired
    private AliOssUtil aliOssUtil;

    @ApiOperation("图片上传")
    @PostMapping("/upload")
    public Result<String> upload(MultipartFile file){
        log.info("文件开始上传：{}",file);

        try {
            // 获取文件原始文件名
            String originalFilename = file.getOriginalFilename();
            // 拿到文件后缀
            String last = originalFilename.substring(originalFilename.lastIndexOf("."));
            // 使用UUID拼接文件名
            String objectName = UUID.randomUUID().toString() + last;
            String upload = aliOssUtil.upload(file.getBytes(), objectName);
            return Result.success(upload);
        } catch (IOException e) {
            log.error("文件上传失败");
            e.printStackTrace();
        }
        return Result.error(MessageConstant.UPLOAD_FAILED);
    }
}
