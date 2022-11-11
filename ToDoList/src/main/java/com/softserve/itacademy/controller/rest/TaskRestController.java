package com.softserve.itacademy.controller.rest;

import com.softserve.itacademy.dto.task.TaskDto;
import com.softserve.itacademy.dto.task.TaskTransformer;
import com.softserve.itacademy.exception.EntityNotCreatedException;
import com.softserve.itacademy.model.Priority;
import com.softserve.itacademy.model.Task;
import com.softserve.itacademy.service.StateService;
import com.softserve.itacademy.service.TaskService;
import com.softserve.itacademy.service.ToDoService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users/{u_id}/todos/{t_id}/tasks")
public class TaskRestController {
    private final TaskService taskService;
    private final ToDoService todoService;
    private final StateService stateService;

    public TaskRestController(TaskService taskService, ToDoService todoService, StateService stateService) {
        this.taskService = taskService;
        this.todoService = todoService;
        this.stateService = stateService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or @toDoServiceImpl.readById(#todoId).owner.email.equals(authentication.name)" +
            " or @toDoServiceImpl.readById(#todoId).collaborators.contains(@userRepository.findByEmail(authentication.name))")
    @ResponseStatus(HttpStatus.OK)
    public List<TaskDto> getAllTasks(@PathVariable(name = "t_id") long todoId,
                                     @PathVariable(name = "u_id") long ownerId) {
        return taskService.getAll()
                .stream()
                .filter(t -> t.getTodo().getId() == todoId)
                .map(TaskDto::new)
                .collect(Collectors.toList());
    }

    @GetMapping("/{task_id}")
    @PreAuthorize("hasRole('ADMIN') or @toDoServiceImpl.readById(#todoId).owner.email.equals(authentication.name)")
    @ResponseStatus(HttpStatus.OK)
    public TaskDto read(@PathVariable(name = "task_id") long taskId,
                        @PathVariable(name = "t_id") long todoId,
                        @PathVariable(name = "u_id") long ownerId) {
        return TaskTransformer.convertToDto(taskService.readById(taskId));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or @toDoServiceImpl.readById(#todoId).owner.email.equals(authentication.name)")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<?> create(@PathVariable(name = "t_id") long todoId,
                                    @PathVariable(name = "u_id") long ownerId,
                                    @Valid @RequestBody TaskDto taskDto,
                                    BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            throw new EntityNotCreatedException(EntityNotCreatedException.errorMessage(bindingResult));
        }
        Task task = TaskTransformer.convertToEntity(taskDto,
                todoService.readById(todoId),
                stateService.getByName("New"));
        task = taskService.create(task);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{task_id}")
                .buildAndExpand(task.getId())
                .toUri();

        return ResponseEntity
                .created(location)
                .body(TaskTransformer.convertToDto(task));
    }

    @PutMapping("/{task_id}")
    @PreAuthorize("hasRole('ADMIN') or @toDoServiceImpl.readById(#todoId).owner.email.equals(authentication.name)")
    public ResponseEntity<?> update(@PathVariable(name = "task_id") long taskId,
                                    @PathVariable(name = "t_id") long todoId,
                                    @PathVariable(name = "u_id") long ownerId,
                                    @Valid @RequestBody TaskDto taskDto,
                                    BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            throw new EntityNotCreatedException(EntityNotCreatedException.errorMessage(bindingResult));
        }
        Task task = taskService.readById(taskId);
        task.setName(taskDto.getName());
        task.setPriority(Priority.valueOf(taskDto.getPriority()));
        task.setState(stateService.readById(taskDto.getStateId()));
        task = taskService.update(task);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .buildAndExpand(task.getId())
                .toUri();

        MultiValueMap<String, String> headers = new HttpHeaders();
        headers.add("location", String.valueOf(location));
        return new ResponseEntity<>(TaskTransformer.convertToDto(task), headers, HttpStatus.OK);
    }

    @DeleteMapping("/{task_id}")
    @PreAuthorize("hasRole('ADMIN') or @toDoServiceImpl.readById(#todoId).owner.email.equals(authentication.name)")
    public ResponseEntity<?> delete(@PathVariable(name = "task_id") long taskId,
                                    @PathVariable(name = "t_id") long todoId,
                                    @PathVariable(name = "u_id") long ownerId) {
        taskService.delete(taskId);
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
