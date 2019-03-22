package com.example.demo;

import org.junit.Test;

import java.io.Serializable;

public class DemoTest {

    @Test
    public void test() throws Exception {
        TestClass testClass = new TestClass();
        testClass.map(User::getUserName);
    }

    public static class User implements Serializable {

        private String userName;
        private String passWord;

        public String getUserName() {
            return userName;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }

        public String getPassWord() {
            return passWord;
        }

        public void setPassWord(String passWord) {
            this.passWord = passWord;
        }
    }

    public static class TestClass {

        public <T, R> void map(LambdaFunction<T, R> lambda) throws Exception {
            SerializedLambda resolve = SerializedLambda.resolve(lambda);
            System.out.println();
        }
    }
}
