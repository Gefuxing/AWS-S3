package com.s3.demo.s3;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.util.IOUtils;


import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;


import java.io.IOException;

@RestController
public class S3Controller {
    // 【你的 access_key】
    static final String AWS_ACCESS_KEY = "XXXX";
    // 【你的 aws_secret_key】
    static final String AWS_SECRET_KEY = " XXXX";
    // 储存桶的名称
    static final String BUCKET_NAME = "XXX";
    // 设置服务器所属地区
    static final Regions REGION = Regions.CN_NORTHWEST_1;
    // 储存路径
    static final String PATH = "test/";

    static final BasicAWSCredentials awsCreds = new BasicAWSCredentials(AWS_ACCESS_KEY, AWS_SECRET_KEY);

    static final AmazonS3 s3 = AmazonS3ClientBuilder.standard()
            .withCredentials(new AWSStaticCredentialsProvider(awsCreds)).withRegion(REGION).build();


    @PostMapping("/s3")
    public  String uploadFileToS3(MultipartFile multipartFile, String type)
            throws AmazonServiceException, SdkClientException, IOException {
        if (multipartFile.isEmpty()) {
            return "文件为空";
        }
        long time = System.currentTimeMillis();
        // 拼接下文件路径，为了不会出现文件覆盖现象，加上时间戳
        String s3FilePath = PATH+time
                + multipartFile.getOriginalFilename();
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(multipartFile.getContentType());
        metadata.setContentLength(multipartFile.getSize());
        // 开始上传文件
        s3.putObject(BUCKET_NAME, s3FilePath, multipartFile.getInputStream(), metadata);
        // 返回文件位置
        return s3FilePath;
    }

    /**
     * 用文件路径获取文件下载地址
     *
     * @param key
     * @return
     */
    @GetMapping("/s3/download")
    public ResponseEntity<byte[]> download(String key) throws IOException {
        GetObjectRequest getObjectRequest = new GetObjectRequest(BUCKET_NAME, key);

        S3Object s3Object =s3.getObject(getObjectRequest);
        S3ObjectInputStream objectInputStream = s3Object.getObjectContent();
        byte[] bytes = IOUtils.toByteArray(objectInputStream);

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        httpHeaders.setContentLength(bytes.length);
        httpHeaders.setContentDispositionFormData("attachment", key);

        return new ResponseEntity<>(bytes, httpHeaders, HttpStatus.OK);
    }
}