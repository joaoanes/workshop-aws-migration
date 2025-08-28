package com.example.todo.webapp.services;

import com.example.todo.dtos.CreateTodoDto;
import com.example.todo.dtos.TodoDto;

import java.util.List;

public interface TodoService {
    TodoDto createTodo(CreateTodoDto createTodoDto, String queueUrl);
    List<TodoDto> getTodos();
}