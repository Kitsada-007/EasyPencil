package com.easypencil;

import java.util.prefs.Preferences;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class ToolBar extends VBox {

    private final ToggleButton toggleMode;
    private final ToggleButton eraserBtn;
    private final Button undoBtn;
    private final Button clearBtn;

    // ตัวช่วยจัดการการบันทึกค่า Settings แบบถาวร
    private final Preferences prefs = Preferences.userNodeForPackage(Main.class);

    public ToolBar(DrawingCanvas canvas, Stage stage) {
        setAlignment(Pos.TOP_LEFT);
        setPickOnBounds(false);
        setPadding(new Insets(10));

        HBox toolbar = new HBox(8);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.setPadding(new Insets(8, 12, 8, 12));
        toolbar.setStyle("-fx-background-color: rgba(30,30,30,0.85); -fx-background-radius: 12;");

        // 1. ปุ่ม Toggle Mode
        toggleMode = new ToggleButton("✏ Draw");
        toggleMode.setSelected(true);
        toggleMode.setStyle(selectedStyle());
        toggleMode.setOnAction(e -> {
            Pane root = (Pane) canvas.getParent();
            if (toggleMode.isSelected()) {
                toggleMode.setText("✏ Draw");
                toggleMode.setStyle(selectedStyle());
                canvas.setMouseTransparent(false);
                root.setStyle("-fx-background-color: rgba(255, 255, 255, 0.01);");
            } else {
                toggleMode.setText("👁 View");
                toggleMode.setStyle(normalStyle());
                canvas.setMouseTransparent(true);
                root.setStyle("-fx-background-color: transparent;");
            }
        });

        ColorPicker colorPicker = new ColorPicker(Color.RED);
        colorPicker.setOnAction(e -> canvas.setBrushColor(colorPicker.getValue()));

        Slider sizeSlider = new Slider(2, 20, 4);
        sizeSlider.valueProperty().addListener((obs, oldVal, newVal) -> canvas.setBrushSize(newVal.doubleValue()));

        // 2. ปุ่ม Eraser
        eraserBtn = new ToggleButton("🧽 Eraser");
        eraserBtn.setStyle(normalStyle());
        eraserBtn.setOnAction(e -> {
            if (eraserBtn.isSelected()) {
                eraserBtn.setStyle(selectedStyle());
                canvas.setEraser(true);
            } else {
                eraserBtn.setStyle(normalStyle());
                canvas.setEraser(false);
            }
        });

        // 3. ปุ่ม Undo และ Clear
        undoBtn = new Button("↩ Undo");
        undoBtn.setStyle(normalStyle());
        undoBtn.setOnAction(e -> canvas.undo());

        clearBtn = new Button("🗑 Clear");
        clearBtn.setStyle(normalStyle());
        clearBtn.setOnAction(e -> canvas.clearCanvas());

        // --- ปุ่มใหม่: Settings ---
        Button settingsBtn = new Button("⚙");
        settingsBtn.setStyle(normalStyle());
        settingsBtn.setOnAction(e -> openSettingsDialog(stage.getScene()));

        // ปุ่ม Close
        Button closeBtn = new Button("✕");
        closeBtn.setStyle("-fx-background-color: #c0392b; -fx-text-fill: white; -fx-background-radius: 6; -fx-font-size: 13px;");
        closeBtn.setOnAction(e -> stage.close());

        toolbar.getChildren().addAll(
                toggleMode,
                new Separator(javafx.geometry.Orientation.VERTICAL),
                new Label("Color:"), colorPicker,
                new Label("Size:"), sizeSlider,
                new Separator(javafx.geometry.Orientation.VERTICAL),
                eraserBtn, undoBtn, clearBtn,
                new Separator(javafx.geometry.Orientation.VERTICAL),
                settingsBtn, closeBtn
        );

        getChildren().add(toolbar);
    }

    // --- เมธอดผูกคีย์ลัด (ดึงค่าที่ผู้ใช้ตั้งไว้มาใช้) ---
    public void setupShortcuts(Scene scene) {
        scene.getAccelerators().clear(); // ล้างคีย์ลัดเก่าออกก่อน

        // แยกจับ Error ทีละปุ่ม ถ้าปุ่มไหนพัง ปุ่มอื่นจะยังทำงานต่อได้ปกติ
        applyShortcut(scene, "hk_mode", "Ctrl+D", toggleMode);
        applyShortcut(scene, "hk_eraser", "Ctrl+E", eraserBtn);
        applyShortcut(scene, "hk_undo", "Ctrl+Z", undoBtn);
        applyShortcut(scene, "hk_clear", "Delete", clearBtn);
    }

    private void applyShortcut(Scene scene, String prefKey, String defaultKey, javafx.scene.control.ButtonBase btn) {
        String keyStr = prefs.get(prefKey, defaultKey);
        try {
            // พยายามตั้งค่าคีย์ลัดตามที่ผู้ใช้เซฟไว้
            scene.getAccelerators().put(KeyCombination.valueOf(keyStr), () -> btn.fire());
        } catch (Exception ex) {
            // ถ้าคีย์ลัดที่เซฟไว้เป็นรูปแบบที่ผิดปกติ (บั๊ก) ให้กลับไปใช้ค่าเริ่มต้นแทน
            System.out.println("❌ รูปแบบคีย์ลัดมีปัญหา: " + keyStr + " -> ใช้ค่าเริ่มต้น: " + defaultKey);
            scene.getAccelerators().put(KeyCombination.valueOf(defaultKey), () -> btn.fire());
            prefs.put(prefKey, defaultKey); // บันทึกค่าเริ่มต้นกลับไปทับค่าที่พัง
        }
    }

    // --- หน้าต่างตั้งค่าคีย์ลัด ---
    private void openSettingsDialog(Scene mainScene) {
        Stage dialog = new Stage();

        dialog.initOwner(mainScene.getWindow());
        dialog.setAlwaysOnTop(true);

        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Hotkey Settings");

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20));
        grid.setHgap(10);
        grid.setVgap(15);

        // สร้างช่องกรอกคีย์ลัด
        TextField modeField = createHotkeyField(prefs.get("hk_mode", "Ctrl+D"));
        TextField eraserField = createHotkeyField(prefs.get("hk_eraser", "Ctrl+E"));
        TextField undoField = createHotkeyField(prefs.get("hk_undo", "Ctrl+Z"));
        TextField clearField = createHotkeyField(prefs.get("hk_clear", "Delete"));

        grid.addRow(0, new Label("Draw / View Mode:"), modeField);
        grid.addRow(1, new Label("Eraser Mode:"), eraserField);
        grid.addRow(2, new Label("Undo:"), undoField);
        grid.addRow(3, new Label("Clear Canvas:"), clearField);

        Button saveBtn = new Button("Save Settings");
        saveBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white;");
        saveBtn.setOnAction(e -> {
            // บันทึกค่าลง Preferences
            prefs.put("hk_mode", modeField.getText());
            prefs.put("hk_eraser", eraserField.getText());
            prefs.put("hk_undo", undoField.getText());
            prefs.put("hk_clear", clearField.getText());

            // อัปเดตคีย์ลัดในโปรแกรมทันที
            setupShortcuts(mainScene);
            dialog.close();
        });

        VBox layout = new VBox(20, grid, saveBtn);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(10));

        dialog.setScene(new Scene(layout, 300, 250));
        dialog.showAndWait();
    }

    // --- ตัวช่วยสร้างช่องดักจับการกดปุ่มบนคีย์บอร์ด ---
    // --- ตัวช่วยสร้างช่องดักจับการกดปุ่มบนคีย์บอร์ด ---
    private TextField createHotkeyField(String currentKey) {
        TextField field = new TextField(currentKey);
        field.setEditable(false); // ห้ามพิมพ์เอง
        field.setStyle("-fx-background-color: #eee; -fx-cursor: hand;");

        field.setOnKeyPressed(e -> {
            // ถ้ายกดแค่ปุ่ม Ctrl, Shift, Alt ค้างไว้ ให้รอจนกว่าจะกดปุ่มหลัก
            if (e.getCode().isModifierKey()) {
                return;
            }

            // ป้องกันปุ่มแปลกๆ ที่ JavaFX ไม่รู้จัก (เช่น ปุ่มเพิ่มลดเสียงบนคีย์บอร์ด)
            if (e.getCode().isFunctionKey() || e.getCode().isLetterKey() || e.getCode().isDigitKey()
                    || e.getCode() == javafx.scene.input.KeyCode.DELETE) {

                String combo = "";
                if (e.isControlDown()) {
                    combo += "Ctrl+";
                }
                if (e.isShiftDown()) {
                    combo += "Shift+";
                }
                if (e.isAltDown()) {
                    combo += "Alt+";
                }

                combo += e.getCode().name(); // ใช้ .name() จะชัวร์ที่สุดสำหรับ JavaFX
                field.setText(combo);
            }
        });
        return field;
    }

    private String selectedStyle() {
        return "-fx-background-color: #3498db; -fx-text-fill: white; -fx-background-radius: 6; -fx-font-size: 13px;";
    }

    private String normalStyle() {
        return "-fx-background-color: #555; -fx-text-fill: white; -fx-background-radius: 6; -fx-font-size: 13px;";
    }
}
