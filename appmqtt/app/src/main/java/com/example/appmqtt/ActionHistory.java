package com.example.appmqtt;

import java.io.Serializable;

public class ActionHistory implements Serializable {
    private String action; // Hành động
    private String time; // Thời gian (đã thay đổi từ long thành String)
    private static final long serialVersionUID = 1L;

    public ActionHistory(String action, String time) {
        this.action = action;
        this.time = time; // Gán giá trị cho time là kiểu String
    }

    public String getAction() {
        return action;
    }

    public String getTime() {
        return time; // Trả về time là kiểu String
    }
}