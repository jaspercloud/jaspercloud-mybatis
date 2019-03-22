package com.example.demo;

import java.io.Serializable;

public interface LambdaFunction<T, R> extends Serializable {

    R apply(T t);
}