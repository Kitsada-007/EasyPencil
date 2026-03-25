package com.easypencil.Widget;

import com.easypencil.ToolBar;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class HotkeySettings extends Stage {

    public HotkeySettings(ToolBar toolbar) {
        // 🌟 1. แก้ปัญหาค้าง: ระบุเจ้าของหน้าต่างและสั่งให้ลอยบนสุด (Always on Top)
        if (toolbar.getScene() != null) {
            this.initOwner(toolbar.getScene().getWindow());
        }
        this.setAlwaysOnTop(true);
        this.initModality(Modality.APPLICATION_MODAL);
        this.initStyle(StageStyle.UTILITY);
        this.setTitle("Hotkey Settings");

        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.CENTER);
        // ใช้สีดำเข้มตามที่คุณส่งมา
        root.setStyle("-fx-background-color: #1a1a1a; -fx-border-color: #333; -fx-border-width: 1;");

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(10);
        grid.setAlignment(Pos.CENTER);

        // รายการคำสั่ง
        String[] tools = {"PEN", "HIGHLIGHT", "TEXT", "ERASER", "UNDO", "SAVE"};
        int row = 0;

        for (String tool : tools) {
            Label nameLabel = new Label(tool + ":");
            nameLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");

            // ดึง Key ปัจจุบันจาก Toolbar มาโชว์
            Button keyBtn = new Button(toolbar.getHotkey(tool).toString());
            keyBtn.setStyle("-fx-background-color: #333; -fx-text-fill: #E91E63; -fx-min-width: 90; -fx-background-radius: 5; -fx-cursor: hand;");

            // 🌟 ระบบดักจับการกดปุ่ม (Key Listener)
            keyBtn.setOnAction(e -> {
                keyBtn.setText("...");
                keyBtn.setStyle("-fx-background-color: #E91E63; -fx-text-fill: white; -fx-min-width: 90;");

                keyBtn.setOnKeyPressed(keyEvent -> {
                    KeyCode newCode = keyEvent.getCode();
                    toolbar.setHotkey(tool, newCode); // อัปเดตค่ากลับไปที่ Toolbar
                    keyBtn.setText(newCode.toString());
                    keyBtn.setStyle("-fx-background-color: #333; -fx-text-fill: #E91E63; -fx-min-width: 90;");

                    // 🌟 สำคัญ: ป้องกันไม่ให้ Key หลุดไปทำงานที่หน้า Canvas หลัก
                    keyEvent.consume();
                    keyBtn.setOnKeyPressed(null);
                });
            });

            grid.add(nameLabel, 0, row);
            grid.add(keyBtn, 1, row);
            row++;
        }

        // ปุ่มปิด
        Button doneBtn = new Button("Save & Close");
        doneBtn.setStyle("-fx-background-color: #444; -fx-text-fill: white; -fx-padding: 8 25; -fx-background-radius: 20; -fx-cursor: hand;");

        doneBtn.setOnAction(e -> {
            // 🌟 บังคับให้หน้าจอหลักกลับมา Focus อีกครั้ง
            if (toolbar.getScene() != null) {
                toolbar.getScene().getRoot().requestFocus();
            }
            this.close();
        });

        root.getChildren().addAll(grid, doneBtn);

        // 🌟 แถม: กดปุ่ม ESC เพื่อปิดหน้าต่างนี้ได้ด้วย
        Scene scene = new Scene(root);
        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
                doneBtn.fire();
            }
        });

        this.setScene(scene);
    }
}
