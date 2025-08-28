package com.example.todo.lambda.dagger;

import com.example.todo.lambda.SqsHandler;
import dagger.Component;

import javax.inject.Singleton;

@Singleton
@Component(modules = {ClientsModule.class, AppModule.class})
public interface SqsHandlerComponent {
    void inject(SqsHandler handler);
}
