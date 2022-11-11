package com.softserve.itacademy.dto.todo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ToDoRequest {
    @NotBlank(message = "The 'title' cannot be empty")
    private String title;
}
