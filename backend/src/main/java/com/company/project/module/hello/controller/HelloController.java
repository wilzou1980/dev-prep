package com.company.project.module.hello.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/hello")
@Tag(name = "Hello", description = "Hello World 示例接口")
public class HelloController {

    @GetMapping(produces = "text/plain;charset=UTF-8")
    @Operation(summary = "Hello World", description = "固定返回 hello world，不接受任何参数")
    public String hello() {
        return "hello world";
    }
}
