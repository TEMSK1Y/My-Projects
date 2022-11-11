package com.softserve.itacademy.controller.rest;

import com.softserve.itacademy.dto.todo.CollaboratorRequest;
import com.softserve.itacademy.dto.todo.CollaboratorResponse;
import com.softserve.itacademy.dto.todo.ToDoRequest;
import com.softserve.itacademy.dto.todo.ToDoResponse;
import com.softserve.itacademy.exception.EntityNotCreatedException;
import com.softserve.itacademy.model.ToDo;
import com.softserve.itacademy.model.User;
import com.softserve.itacademy.service.ToDoService;
import com.softserve.itacademy.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.Valid;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@Slf4j
@RequestMapping("/api/users/{u_id}/todos")
public class ToDoRestController {

    private final ToDoService toDoService;
    private final UserService userService;

    @Autowired
    public ToDoRestController(ToDoService toDoService, UserService userService) {
        this.toDoService = toDoService;
        this.userService = userService;
    }

    @GetMapping()
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER') and authentication.principal.id == #id")
    List<ToDoResponse> getUsersTodos(@PathVariable(name = "u_id") long id) {
        return toDoService.getByUserId(id).stream()
                .map(ToDoResponse::new)
                .collect(Collectors.toList());
    }

    @GetMapping("/{t_id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER') and authentication.principal.id == #ownerId")
    public ResponseEntity<?> read(@PathVariable(name = "u_id") long ownerId,
                                  @PathVariable(name = "t_id") long t_id) {
        return ResponseEntity.ok(new ToDoResponse(toDoService.readById(t_id)));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER') and authentication.principal.id == #ownerId")
    ResponseEntity<?> create(@PathVariable(name = "u_id") long ownerId,
                             @Valid @RequestBody ToDoRequest todoRequest, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            throw new EntityNotCreatedException(EntityNotCreatedException.errorMessage(bindingResult));
        }

        ToDo toDo = new ToDo();
        toDo.setTitle(todoRequest.getTitle());
        toDo.setCreatedAt(LocalDateTime.now());
        toDo.setOwner(userService.readById(ownerId));
        toDo = toDoService.create(toDo);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{t_id}")
                .buildAndExpand(toDo.getId())
                .toUri();

        return ResponseEntity
                .created(location)
                .body(new ToDoResponse(toDo));
    }

    @PutMapping("/{t_id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER') and authentication.principal.id == #ownerId")
    ResponseEntity<?> update(@PathVariable(name = "u_id") long ownerId,
                             @PathVariable(name = "t_id") long oldTodoId,
                             @Valid @RequestBody ToDoRequest newTodo, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            throw new EntityNotCreatedException(EntityNotCreatedException.errorMessage(bindingResult));
        }
        ToDo newToDo = toDoService.readById(oldTodoId);
        newToDo.setTitle(newTodo.getTitle());
        toDoService.update(newToDo);
        return new ResponseEntity<>(new ToDoResponse(newToDo), HttpStatus.OK);
    }

    @DeleteMapping("/{t_id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER') and authentication.principal.id == #ownerId")
    ResponseEntity<?> delete(@PathVariable(name = "u_id") long ownerId,
                             @PathVariable(name = "t_id") long todoId) {
        toDoService.delete(todoId);
        return new ResponseEntity<>("", HttpStatus.OK);
    }

    @GetMapping("/{t_id}/collaborators")
    @PreAuthorize("hasRole('ADMIN') or @toDoServiceImpl.readById(#todoId).owner.email.equals(authentication.name)" +
            " or @toDoServiceImpl.readById(#todoId).collaborators.contains(@userRepository.findByEmail(authentication.name))")
    public List<?> getTodoCollaborators(@PathVariable(name = "t_id") long todoId) {
        ToDo toDo = toDoService.readById(todoId);

        return toDo.getCollaborators().stream()
                .map(CollaboratorResponse::new)
                .collect(Collectors.toList());
    }

    @PostMapping("/{t_id}/collaborators")
    @PreAuthorize("hasRole('ADMIN') or @toDoServiceImpl.readById(#todoId).owner.email.equals(authentication.name)")
    public ResponseEntity<?> addCollaborator(@RequestBody CollaboratorRequest collaboratorRequest,
                                             @PathVariable(name = "t_id") long todoId) {
        boolean isCollaboratorExist = false;

        ToDo toDo = toDoService.readById(todoId);
        List<User> collaborators = toDo.getCollaborators();

        for (User user : collaborators)
            if (user.getId() == collaboratorRequest.getCollaborator_id()) {
                isCollaboratorExist = true;
                break;
            }
        if (isCollaboratorExist) return new ResponseEntity<>("", HttpStatus.CONFLICT);

        collaborators.add(userService.readById(collaboratorRequest.getCollaborator_id()));
        toDo.setCollaborators(collaborators);

        toDoService.update(toDo);

        return new ResponseEntity<>("", HttpStatus.CREATED);
    }

    @DeleteMapping("/{t_id}/collaborators/{c_id}")
    @PreAuthorize("hasRole('ADMIN') or @toDoServiceImpl.readById(#todoId).owner.email.equals(authentication.name)")
    public ResponseEntity<?> removeCollaborator(@PathVariable(name = "t_id") long todoId,
                                                @PathVariable(name = "c_id") long collaboratorId) {
        boolean isCollaboratorExist = false;

        ToDo toDo = toDoService.readById(todoId);
        List<User> collaborators = toDo.getCollaborators();

        for (User user : collaborators)
            if (user.getId() == collaboratorId) {
                isCollaboratorExist = true;
                break;
            }
        if (!isCollaboratorExist) return new ResponseEntity<>("", HttpStatus.NOT_FOUND);

        collaborators.remove(userService.readById(collaboratorId));
        toDo.setCollaborators(collaborators);
        toDoService.update(toDo);

        return new ResponseEntity<>("", HttpStatus.OK);
    }
}
