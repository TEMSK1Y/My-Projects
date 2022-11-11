package com.softserve.itacademy.dto.task;

import com.softserve.itacademy.model.Task;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class TaskDto {
    private long id;

    @NotBlank(message = "The 'name' cannot be empty")
    private String name;

    @NotNull
    private String priority;

    @NotNull
    private long todoId;

    @NotNull
    private long stateId;

    public TaskDto(Task task) {
        this.id = task.getId();
        this.name = task.getName();
        this.priority = task.getPriority().toString();
        this.todoId = task.getTodo().getId();
        this.stateId = task.getState().getId();
    }
}
