package com.easypencil;

import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinUser;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class Main extends Application {

    public static HWND hwnd;

    @Override
    public void start(Stage stage) {
        double w = javafx.stage.Screen.getPrimary().getBounds().getWidth();
        double h = javafx.stage.Screen.getPrimary().getBounds().getHeight();

        DrawingCanvas canvas = new DrawingCanvas();
        ToolBar toolbar = new ToolBar(canvas, stage);

        // Canvas + Toolbar อยู่ใน Stage เดียวกัน เพื่อไม่ให้เกิด z-order conflict
        Pane root = new Pane(canvas, toolbar);
        root.setStyle("-fx-background-color: rgba(255, 255, 255, 0.01);");

        Scene scene = new Scene(root, w, h);
        scene.setFill(Color.TRANSPARENT);
        toolbar.setupShortcuts(scene);

        root.setFocusTraversable(true);
        root.requestFocus();

        stage.setTitle("EasyPencil");
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.setAlwaysOnTop(true);
        stage.setScene(scene);
        stage.setX(0);
        stage.setY(0);
        stage.show();

        // หา HWND แล้ว set draw mode ครั้งแรก (ลบ WS_EX_TRANSPARENT)
        new Thread(() -> {
            try { Thread.sleep(500); } catch (InterruptedException ex) { }
            Platform.runLater(() -> {
                hwnd = User32.INSTANCE.FindWindow(null, "EasyPencil");
                if (hwnd != null) {
                    System.out.println("Window found: " + hwnd);
                    setDrawMode(true);
                } else {
                    System.out.println("Window not found!");
                }
            });
        }).start();
    }

    /**
     * Draw mode  = true  → ลบ WS_EX_TRANSPARENT → window รับ mouse events
     * View mode  = false → เพิ่ม WS_EX_TRANSPARENT → window click-through ทั้งหมด
     *
     * หมายเหตุ: setDrawMode(false) ใช้ได้เฉพาะถ้า Toolbar อยู่ใน Stage แยก
     * ในแบบ Single-Stage ให้ใช้ background-color transparent แทน (ดู ToolBar.java)
     */
    public static void setDrawMode(boolean drawMode) {
        if (hwnd == null) return;
        int exStyle = User32.INSTANCE.GetWindowLong(hwnd, WinUser.GWL_EXSTYLE);
        if (drawMode) {
            User32.INSTANCE.SetWindowLong(hwnd, WinUser.GWL_EXSTYLE,
                    exStyle & ~WinUser.WS_EX_TRANSPARENT);
        } else {
            User32.INSTANCE.SetWindowLong(hwnd, WinUser.GWL_EXSTYLE,
                    exStyle | WinUser.WS_EX_LAYERED | WinUser.WS_EX_TRANSPARENT);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
