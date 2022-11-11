package com.softserve.itacademy.dto.todo;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.softserve.itacademy.model.ToDo;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class ToDoResponse {
    private Long id;
    private String title;
    private String createdAt;
    private String owner;

    public ToDoResponse(ToDo toDo) {
        this.id = toDo.getId();
        this.title = toDo.getTitle();
        this.createdAt = String.valueOf(toDo.getCreatedAt());
        this.owner = toDo.getOwner().getEmail();
    }
}
