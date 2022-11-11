package com.softserve.itacademy.controller.view;

import com.softserve.itacademy.model.Task;
import com.softserve.itacademy.model.ToDo;
import com.softserve.itacademy.model.User;
import com.softserve.itacademy.security.UserDetailsServiceImpl;
import com.softserve.itacademy.service.TaskService;
import com.softserve.itacademy.service.ToDoService;
import com.softserve.itacademy.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/todos")
public class ToDoController {

    private final ToDoService todoService;
    private final TaskService taskService;
    private final UserService userService;
    private final UserDetailsServiceImpl userDetailsServiceImpl;

    public ToDoController(ToDoService todoService, TaskService taskService, UserService userService,
                          UserDetailsServiceImpl userDetailsServiceImpl) {
        this.todoService = todoService;
        this.taskService = taskService;
        this.userService = userService;
        this.userDetailsServiceImpl = userDetailsServiceImpl;
    }

    @GetMapping("/**")
    public String error404() {
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Page not found");
    }

    @GetMapping("/create/users/{owner_id}")
    @PreAuthorize("hasRole('ADMIN') or @userServiceImpl.readById(#ownerId).email.equals(authentication.name)")
    public String create(@PathVariable("owner_id") long ownerId, Model model) {
        model.addAttribute("owner", userDetailsServiceImpl.getSecurityUser());
        model.addAttribute("todo", new ToDo());
        model.addAttribute("ownerId", ownerId);
        return "create-todo";
    }

    @PostMapping("/create/users/{owner_id}")
    public String create(@PathVariable("owner_id") long ownerId, @Validated @ModelAttribute("todo") ToDo todo, BindingResult result) {
        if (result.hasErrors()) {
            return "create-todo";
        }
        todo.setCreatedAt(LocalDateTime.now());
        todo.setOwner(userService.readById(ownerId));
        todoService.create(todo);
        return "redirect:/todos/all/users/" + ownerId;
    }

    @GetMapping("/{id}/tasks")
    @PreAuthorize("hasRole('ADMIN') or " +
            "@toDoServiceImpl.readById(#id).collaborators.contains(@userRepository.findByEmail(authentication.name)) " +
            "or @toDoServiceImpl.readById(#id).owner.email.equals(authentication.name)")
    public String read(@PathVariable long id, Model model) {
        model.addAttribute("owner", userDetailsServiceImpl.getSecurityUser());
        ToDo todo = todoService.readById(id);
        List<Task> tasks = taskService.getByTodoId(id);
        List<User> users = userService.getAll().stream()
                .filter(user -> user.getId() != todo.getOwner().getId()).collect(Collectors.toList());
        model.addAttribute("todo", todo);
        model.addAttribute("tasks", tasks);
        model.addAttribute("users", users);
        return "todo-tasks";
    }

    @GetMapping("/{todo_id}/update/users/{owner_id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER') and @toDoServiceImpl.readById(#todoId).owner.email.equals(authentication.name)")
    public String update(@PathVariable("todo_id") long todoId, @PathVariable("owner_id") long ownerId, Model model) {
        model.addAttribute("owner", userDetailsServiceImpl.getSecurityUser());
        ToDo todo = todoService.readById(todoId);
        model.addAttribute("todo", todo);
        return "update-todo";
    }

    @PostMapping("/{todo_id}/update/users/{owner_id}")
    public String update(@PathVariable("todo_id") long todoId, @PathVariable("owner_id") long ownerId,
                         @Validated @ModelAttribute("todo") ToDo todo, BindingResult result) {
        if (result.hasErrors()) {
            todo.setOwner(userService.readById(ownerId));
            return "update-todo";
        }
        ToDo oldTodo = todoService.readById(todoId);
        todo.setOwner(oldTodo.getOwner());
        todo.setCollaborators(oldTodo.getCollaborators());
        todoService.update(todo);
        return "redirect:/todos/all/users/" + ownerId;
    }

    @GetMapping("/{todo_id}/delete/users/{owner_id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER') and @toDoServiceImpl.readById(#todoId).owner.email.equals(authentication.name)")
    public String delete(@PathVariable("todo_id") long todoId, @PathVariable("owner_id") long ownerId) {
        todoService.delete(todoId);
        return "redirect:/todos/all/users/" + ownerId;
    }

    @GetMapping("/all/users/{user_id}")
    @PreAuthorize("hasRole('ADMIN') or @userServiceImpl.readById(#userId).email.equals(authentication.name)")
    public String getAll(@PathVariable("user_id") long userId, Model model) {
        model.addAttribute("owner", userDetailsServiceImpl.getSecurityUser());
        List<ToDo> todos = todoService.getByUserId(userId);
        model.addAttribute("todos", todos);
        model.addAttribute("user", userService.readById(userId));
        return "todos-user";
    }

    @GetMapping("/{id}/add")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER') and @toDoServiceImpl.readById(#id).owner.email.equals(authentication.name)")
    public String addCollaborator(@PathVariable long id, @RequestParam("user_id") long userId) {
        ToDo todo = todoService.readById(id);
        List<User> collaborators = todo.getCollaborators();
        collaborators.add(userService.readById(userId));
        todo.setCollaborators(collaborators);
        todoService.update(todo);
        return "redirect:/todos/" + id + "/tasks";
    }

    @GetMapping("/{id}/remove")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER') and @toDoServiceImpl.readById(#id).owner.email.equals(authentication.name)")
    public String removeCollaborator(@PathVariable long id, @RequestParam("user_id") long userId) {
        ToDo todo = todoService.readById(id);
        List<User> collaborators = todo.getCollaborators();
        collaborators.remove(userService.readById(userId));
        todo.setCollaborators(collaborators);
        todoService.update(todo);
        return "redirect:/todos/" + id + "/tasks";
    }
}
