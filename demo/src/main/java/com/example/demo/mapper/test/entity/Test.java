package com.example.demo.mapper.test.entity;

import com.jaspercloud.mybatis.support.plus.annotation.SelectKey;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity(name = "test")
public class Test {

    @Id
    @SelectKey(statement = "select nextval('seq_test')", before = true)
    private Long id;
    private String content;
    private String nickName;
    @Column(name = "username")
    private String userName;
    @Column(name = "pass_wd")
    private String password;
    @SelectKey(statement = "select nextval('seq_test')", before = true)
    @Column(name = "seq_test")
    private Long seq;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Long getSeq() {
        return seq;
    }

    public void setSeq(Long seq) {
        this.seq = seq;
    }

    public Test() {
    }
}
