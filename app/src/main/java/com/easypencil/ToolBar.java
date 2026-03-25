package com.easypencil;

import java.io.File;
import java.util.HashMap;
import java.util.Map; // 🌟 เรียกใช้ Widget หน้าต่างตั้งค่า

import com.easypencil.Widget.ActionButton;
import com.easypencil.Widget.HotkeySettings;
import com.easypencil.Widget.ToolButton;

import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.Slider;
import javafx.scene.control.TextInputControl;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class ToolBar extends VBox {

    private double xOffset = 0;
    private double yOffset = 0;
    private boolean isHorizontal = true;
    private boolean isDarkMode = true; // เริ่มต้นที่สีดำแบบ Epic Pen

    // UI Elements
    private Label dragHandle;
    private ActionButton rotateBtn;
    private ToolButton toggleMode;

    private ToolButton penBtn;
    private ToolButton highlightBtn;
    private ToolButton textBtn;
    private ToolButton eraserBtn;

    private ActionButton undoBtn;
    private ActionButton clearBtn;
    private ActionButton saveBtn;
    private ActionButton settingsBtn; // 🌟 ปุ่มเฟืองตั้งค่า
    private ActionButton closeBtn;

    private Label colorLabel;
    private ColorPicker colorPicker;
    private Label sizeLabel;
    private Slider sizeSlider;

    // 🌟 ระบบเก็บข้อมูลคีย์ลัด
    private final Map<String, KeyCode> hotkeys = new HashMap<>();

    public ToolBar(DrawingCanvas canvas, Stage stage) {
        setAlignment(Pos.TOP_LEFT);
        setPickOnBounds(false);
        setPadding(new Insets(15));

        // 1. ตั้งค่าคีย์ลัดเริ่มต้น
        initDefaultHotkeys();

        // 2. สร้างส่วนประกอบหลัก
        dragHandle = new Label("⣿");
        dragHandle.setTextFill(Color.web("#555555"));
        dragHandle.setStyle("-fx-font-size: 18px; -fx-cursor: move;");

        rotateBtn = new ActionButton("🔄",
                "-fx-background-color: transparent; -fx-text-fill: #999999; -fx-font-size: 14px; -fx-cursor: hand; -fx-background-radius: 20;",
                "-fx-background-color: #333333; -fx-text-fill: white; -fx-font-size: 14px; -fx-cursor: hand; -fx-background-radius: 20;"
        );
        rotateBtn.setOnAction(e -> {
            isHorizontal = !isHorizontal;
            buildLayout();
        });

        toggleMode = new ToolButton("✏ Draw", null);
        toggleMode.setActive(true);
        toggleMode.setOnAction(e -> {
            if (toggleMode.isSelected()) {
                toggleMode.setText("✏ Draw");
                toggleMode.setActive(true);
                canvas.setMouseTransparent(false);
                Main.setDrawMode(true);
            } else {
                toggleMode.setText("👁 View");
                toggleMode.setActive(false);
                canvas.setMouseTransparent(true);
                Main.setDrawMode(false);
            }
        });

        // 3. สร้างปุ่มเครื่องมือ (ToolButtons)
        penBtn = new ToolButton("Pen", "pencil.png");
        highlightBtn = new ToolButton("Highlight", "highlighter.png");
        textBtn = new ToolButton("Text", "text_icon.png");
        eraserBtn = new ToolButton("Eraser", "eraser.png");

        setActiveTool(penBtn);
        canvas.setPenMode();

        penBtn.setOnAction(e -> {
            setActiveTool(penBtn);
            canvas.setPenMode();
        });
        highlightBtn.setOnAction(e -> {
            setActiveTool(highlightBtn);
            canvas.setHighlightMode();
        });
        textBtn.setOnAction(e -> {
            setActiveTool(textBtn);
            canvas.setTextMode();
        });
        eraserBtn.setOnAction(e -> {
            setActiveTool(eraserBtn);
            canvas.setEraserMode();
        });

        // 4. ส่วนเลือกสีและขนาด
        colorLabel = new Label("Color");
        colorLabel.setTextFill(Color.web("#FFFFFF"));
        colorLabel.setStyle("-fx-font-size: 11px;");

        colorPicker = new ColorPicker(Color.web("#E91E63"));
        colorPicker.setStyle("-fx-color-label-visible: false; -fx-background-color: #2b2b2b; -fx-background-radius: 20; -fx-cursor: hand;");
        colorPicker.setOnAction(e -> {
            canvas.setBrushColor(colorPicker.getValue());
            if (canvas.isEraser()) {
                penBtn.fire();
            }
        });
        canvas.setBrushColor(colorPicker.getValue());

        sizeLabel = new Label("Size: 4");
        sizeLabel.setTextFill(Color.web("#FFFFFF"));
        sizeLabel.setStyle("-fx-font-size: 11px;");
        sizeLabel.setPrefWidth(45);

        sizeSlider = new Slider(1, 100, 4);
        sizeSlider.setPrefWidth(80);
        sizeSlider.setStyle("-fx-cursor: hand;");
        sizeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            canvas.setBrushSize(newVal.doubleValue());
            sizeLabel.setText("Size: " + String.format("%.0f", newVal.doubleValue()));
        });

        // 5. ปุ่มคำสั่ง (ActionButtons)
        undoBtn = new ActionButton("↩ Undo");
        undoBtn.setOnAction(e -> canvas.undo());

        clearBtn = new ActionButton("🗑 Clear");
        clearBtn.setOnAction(e -> canvas.clearCanvas());

        saveBtn = new ActionButton("💾 Save");
        saveBtn.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Image As");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG Files", "*.png"));
            File file = fileChooser.showSaveDialog(stage);
            if (file != null) {
                canvas.saveAsPng(file);
            }
        });

        settingsBtn = new ActionButton("⚙");
        settingsBtn.setOnAction(e -> {
            HotkeySettings settingsWindow = new HotkeySettings(this);
            settingsWindow.showAndWait();
        });

        closeBtn = new ActionButton("✕",
                "-fx-background-color: transparent; -fx-text-fill: #ff4d4d; -fx-background-radius: 20; -fx-font-size: 14px; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 6 12;",
                "-fx-background-color: #ff4d4d; -fx-text-fill: white; -fx-background-radius: 20; -fx-font-size: 14px; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 6 12;"
        );
        closeBtn.setOnAction(e -> stage.close());

        buildLayout();
    }

    private void initDefaultHotkeys() {
        hotkeys.put("PEN", KeyCode.P);
        hotkeys.put("HIGHLIGHT", KeyCode.H);
        hotkeys.put("TEXT", KeyCode.T);
        hotkeys.put("ERASER", KeyCode.E);
        hotkeys.put("UNDO", KeyCode.Z);
        hotkeys.put("SAVE", KeyCode.S);
    }

    public void setHotkey(String toolName, KeyCode newKey) {
        hotkeys.put(toolName.toUpperCase(), newKey);
    }

    public KeyCode getHotkey(String toolName) {
        return hotkeys.get(toolName.toUpperCase());
    }

    private void setActiveTool(ToolButton activeBtn) {
        ToolButton[] tools = {penBtn, highlightBtn, textBtn, eraserBtn};
        for (ToolButton btn : tools) {
            btn.setActive(btn == activeBtn);
        }
    }

    private void buildLayout() {
        Pane container;
        Orientation sepOrientation;

        if (isHorizontal) {
            HBox box = new HBox(6);
            box.setAlignment(Pos.CENTER_LEFT);
            container = box;
            sepOrientation = Orientation.VERTICAL;
            dragHandle.setText("⣿");
            dragHandle.setPadding(new Insets(0, 5, 0, 0));
        } else {
            VBox box = new VBox(6);
            box.setAlignment(Pos.TOP_CENTER);
            container = box;
            sepOrientation = Orientation.HORIZONTAL;
            dragHandle.setText("⠛");
            dragHandle.setPadding(new Insets(0, 0, 5, 0));
        }

        container.setPadding(new Insets(8, 14, 8, 14));
        container.setStyle(
                "-fx-background-color: #1a1a1a;"
                + "-fx-background-radius: 30;"
                + "-fx-border-color: #333333;"
                + "-fx-border-radius: 30;"
                + "-fx-border-width: 1;"
                + "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.6), 12, 0, 0, 6);"
        );

        container.setOnMousePressed(e -> {
            xOffset = e.getSceneX() - this.getLayoutX();
            yOffset = e.getSceneY() - this.getLayoutY();
        });
        container.setOnMouseDragged(e -> {
            this.setLayoutX(e.getSceneX() - xOffset);
            this.setLayoutY(e.getSceneY() - yOffset);
        });

        container.getChildren().addAll(
                dragHandle, rotateBtn, getStyledSeparator(sepOrientation),
                toggleMode, getStyledSeparator(sepOrientation),
                penBtn, highlightBtn, textBtn, eraserBtn, getStyledSeparator(sepOrientation),
                colorLabel, colorPicker, sizeLabel, sizeSlider, getStyledSeparator(sepOrientation),
                undoBtn, clearBtn, saveBtn, settingsBtn, getStyledSeparator(sepOrientation),
                closeBtn
        );

        this.getChildren().clear();
        this.getChildren().add(container);
    }

    private Separator getStyledSeparator(Orientation orientation) {
        Separator sep = new Separator(orientation);
        sep.setStyle("-fx-opacity: 0.15; -fx-background-color: #ffffff;");
        return sep;
    }

    public void setupShortcuts(Scene scene) {
        scene.setOnKeyPressed(e -> {
            // ถ้ากำลังพิมพ์ในกล่องข้อความ ให้หยุดคีย์ลัด
            if (e.getTarget() instanceof TextInputControl) {
                return;
            }

            // ถ้าหน้าต่างหลัก (Scene) ไม่ได้ถูก Focus อยู่ ก็ไม่ต้องทำงาน
            if (!scene.getWindow().isFocused()) {
                return;
            }

            KeyCode code = e.getCode();

            // ใช้ if-else เช็คปุ่มจาก Map เหมือนเดิม
            if (code == hotkeys.get("PEN")) {
                penBtn.fire();
            } else if (code == hotkeys.get("HIGHLIGHT")) {
                highlightBtn.fire();
            } else if (code == hotkeys.get("TEXT")) {
                textBtn.fire();
            } else if (code == hotkeys.get("ERASER")) {
                eraserBtn.fire();
            }

            if (e.isControlDown()) {
                if (code == hotkeys.get("UNDO")) {
                    undoBtn.fire();
                } else if (code == hotkeys.get("SAVE")) {
                    saveBtn.fire();
                }
            }
        });
    }
}
