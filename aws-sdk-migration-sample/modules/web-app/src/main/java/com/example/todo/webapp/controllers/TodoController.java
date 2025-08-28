package com.example.todo.webapp.controllers;

import com.amazonaws.services.sqs.AmazonSQS;
import com.example.todo.dtos.CreateTodoDto;
import com.example.todo.dtos.TodoDto;
import com.example.todo.webapp.config.DaggerBridge;
import com.example.todo.webapp.services.TodoService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/todos")
public class TodoController {

    private final TodoService todoService;
    private final AmazonSQS sqs;

    // Constructor for production use by Spring
    public TodoController() {
        this.todoService = DaggerBridge.getAppComponent().todoService();
        this.sqs = DaggerBridge.getAppComponent().amazonSQS();
    }

    // Package-private constructor for testing
    TodoController(TodoService todoService, AmazonSQS sqs) {
        this.todoService = todoService;
        this.sqs = sqs;
    }

    @PostMapping
    public TodoDto createTodo(@RequestBody CreateTodoDto createTodoDto) {
        String queueUrl = sqs.getQueueUrl("todo-queue").getQueueUrl();
        return todoService.createTodo(createTodoDto, queueUrl);
    }

    @GetMapping
    public List<TodoDto> getTodos() {
        return todoService.getTodos();
    }
}