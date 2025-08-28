package com.example.todo.webapp.config;

import com.amazonaws.services.sqs.AmazonSQS;
import com.example.todo.webapp.services.TodoService;
import dagger.Component;

import javax.inject.Singleton;

@Singleton
@Component(modules = {AppModule.class, ClientsModule.class})
public interface AppComponent {
    TodoService todoService();
    AmazonSQS amazonSQS();
}
