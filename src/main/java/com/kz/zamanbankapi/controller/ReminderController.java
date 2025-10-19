package com.kz.zamanbankapi.controller;

import com.kz.zamanbankapi.dto.AishaAdvice;
import com.kz.zamanbankapi.service.ReminderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/reminders")
public class ReminderController {
    private final ReminderService reminderService;

    @GetMapping
    public AishaAdvice getReminders() {
        return reminderService.getReminders();
    }
}
