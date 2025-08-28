package com.example.todo.lambda.dagger;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;

@Module
public class ClientsModule {

    @Provides
    @Singleton
    AmazonS3 provideAmazonS3() {
        // This is a placeholder for local development.
        // In a real application, you would configure this with credentials and a region.
        return AmazonS3ClientBuilder.standard()
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration("http://localhost:4566", "us-east-1"))
                .withPathStyleAccessEnabled(true)
                .build();
    }
}
