package com.ortecfinance.tasklist.validation;

import com.ortecfinance.tasklist.service.TaskService;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;

public class DateInputValidator implements ConstraintValidator<DateInputValid, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        LocalDate.parse(value, TaskService.DATE_FORMAT);
        return true;
    }
}