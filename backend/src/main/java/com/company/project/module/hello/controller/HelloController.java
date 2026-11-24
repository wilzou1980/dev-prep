package com.company.project.module.hello.controller;

import com.company.project.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Size;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/hello")
@Tag(name = "Hello", description = "Hello World 示例接口")
public class HelloController {

    @GetMapping
    @Operation(summary = "打招呼", description = "按名字返回问候语，默认 World")
    public Result<String> hello(
            @Parameter(description = "名字")
            @RequestParam(defaultValue = "World") @Size(max = 50) String name) {
        return Result.ok("Hello, " + name + "!");
    }
}
