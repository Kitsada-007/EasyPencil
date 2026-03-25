package com.easypencil.Widget;

import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public class FloatingTextBox extends VBox {

    private TextArea textArea;
    private double dragOffsetX = 0;
    private double dragOffsetY = 0;
    private double fontSize;

    // รับค่า พิกัด X, Y, สี, ขนาดแปรง และคำสั่งที่จะให้ทำตอนกด Enter (Runnable)
    public FloatingTextBox(double x, double y, Color textColor, double brushSize, Runnable onEnterPressed) {
        this.setLayoutX(x);
        this.setLayoutY(y - 24);
        this.fontSize = Math.max(16, brushSize * 4);

        // 🌟 สร้างแถบจับลาก
        Label moveHandle = new Label("✥ ลากตรงนี้เพื่อย้ายตำแหน่ง");
        moveHandle.setStyle("-fx-background-color: rgba(41, 128, 185, 0.9); -fx-text-fill: white; -fx-padding: 3 8; -fx-cursor: move; -fx-font-size: 12px; -fx-background-radius: 6 6 0 0;");
        moveHandle.setMinHeight(24);
        moveHandle.setMaxHeight(24);
        moveHandle.setMaxWidth(Double.MAX_VALUE);

        moveHandle.setOnMousePressed(e -> {
            dragOffsetX = e.getSceneX() - this.getLayoutX();
            dragOffsetY = e.getSceneY() - this.getLayoutY();
            e.consume();
        });
        moveHandle.setOnMouseDragged(e -> {
            this.setLayoutX(e.getSceneX() - dragOffsetX);
            this.setLayoutY(e.getSceneY() - dragOffsetY);
            e.consume();
        });

        // 🌟 สร้างช่องพิมพ์ข้อความ
        textArea = new TextArea();
        textArea.setWrapText(false);

        String hexColor = String.format("#%02X%02X%02X",
                (int) (textColor.getRed() * 255),
                (int) (textColor.getGreen() * 255),
                (int) (textColor.getBlue() * 255));

        textArea.setStyle(
                "-fx-control-inner-background: rgba(255,255,255,0.8); " +
                "-fx-background-color: transparent; " +
                "-fx-text-fill: " + hexColor + "; " +
                "-fx-font-size: " + fontSize + "px; " +
                "-fx-border-color: #888; -fx-border-style: dashed; -fx-border-width: 0 1 1 1;"
        );

        // คำนวณขนาดกล่องตามตัวอักษร
        Text textMeasurer = new Text();
        textMeasurer.setFont(Font.font("System", fontSize));

        double initialWidth = fontSize * 3;
        double initialHeight = fontSize * 2.5;
        textArea.setPrefWidth(initialWidth);
        textArea.setPrefHeight(initialHeight);

        textArea.textProperty().addListener((obs, oldText, newText) -> {
            textMeasurer.setText(newText.isEmpty() ? "A" : newText);
            double newWidth = textMeasurer.getLayoutBounds().getWidth() + 35;
            double newHeight = textMeasurer.getLayoutBounds().getHeight() + 25;
            textArea.setPrefWidth(Math.max(initialWidth, newWidth));
            textArea.setPrefHeight(Math.max(initialHeight, newHeight));
        });

        // ดักการกด Ctrl + Enter
        textArea.setOnKeyPressed(e -> {
            if (e.isControlDown() && e.getCode() == javafx.scene.input.KeyCode.ENTER) {
                if (onEnterPressed != null) onEnterPressed.run();
            }
        });

        this.getChildren().addAll(moveHandle, textArea);
    }

    // --- ฟังก์ชันส่งค่ากลับไปให้ Canvas เอาไปประทับตรา ---
    public String getText() {
        return textArea.getText();
    }

    public double getStampX() {
        return this.getLayoutX() + 5;
    }

    public double getStampY() {
        return this.getLayoutY() + 24 + fontSize;
    }

    public void focusText() {
        textArea.requestFocus();
    }
}