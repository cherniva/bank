package com.cherniva.blockerservice.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.concurrent.ThreadLocalRandom;

@RestController
@RequestMapping("/blocker/check")
public class BlockerController {
    @GetMapping
    public boolean check() {
        return ThreadLocalRandom.current().nextInt(0, 4) != 0; // 75% true
    }
}
