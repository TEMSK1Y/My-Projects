package com.softserve.itacademy.dto.todo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CollaboratorRequest {
    @NotBlank(message = "The 'id' cannot be empty")
    private Long collaborator_id;
}
