package com.example.order.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

@Service
@RequiredArgsConstructor
public class FileWriterService {

    private final ObjectMapper objectMapper;

    @SneakyThrows
    public void writeToJsonFile(final String fileName, final String content) {
        Files.write(Paths.get("./workdir/".concat(fileName)), content.getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
    }

}
