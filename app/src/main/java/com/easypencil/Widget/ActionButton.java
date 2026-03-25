package com.easypencil.Widget;

import javafx.scene.control.Button;

public class ActionButton extends Button {

    private String normalCss;
    private String hoverCss;

    // 🌟 1. คอนสตรัคเตอร์สำหรับปุ่มทั่วไป (ใช้สีพื้นฐาน)
    public ActionButton(String text) {
        super(text);
        this.normalCss = "-fx-background-color: transparent; -fx-text-fill: #cccccc; -fx-background-radius: 20; -fx-font-size: 12px; -fx-cursor: hand; -fx-padding: 6 12;";
        this.hoverCss = "-fx-background-color: #333333; -fx-text-fill: #ffffff; -fx-background-radius: 20; -fx-font-size: 12px; -fx-cursor: hand; -fx-padding: 6 12;";
        setupStyle();
    }

    // 🌟 2. คอนสตรัคเตอร์สำหรับปุ่มพิเศษ (ส่งสีมาเองได้ เช่น ปุ่ม Close สีแดง)
    public ActionButton(String text, String customNormal, String customHover) {
        super(text);
        this.normalCss = customNormal;
        this.hoverCss = customHover;
        setupStyle();
    }

    private void setupStyle() {
        this.setStyle(normalCss);
        this.setOnMouseEntered(e -> this.setStyle(hoverCss));
        this.setOnMouseExited(e -> this.setStyle(normalCss));
    }
}