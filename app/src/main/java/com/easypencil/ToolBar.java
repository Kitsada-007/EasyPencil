package com.easypencil;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ToolBar extends VBox {

    private double xOffset = 0;
    private double yOffset = 0;
    private boolean isHorizontal = true;
    private boolean isDarkMode = true;

    private Label dragHandle;
    private ActionButton rotateBtn;
    private ToolButton toggleMode;

    // Drawing tools
    private ToolButton penBtn;
    private ToolButton highlightBtn;
    private ToolButton textBtn;
    private ToolButton eraserBtn;

    // Shape tools
    private ToolButton lineBtn;
    private ToolButton rectBtn;
    private ToolButton circleBtn;
    private ToolButton triangleBtn;

    // Action buttons
    private ActionButton undoBtn;
    private ActionButton clearBtn;
    private ActionButton saveBtn;
    private ActionButton settingsBtn;
    private ActionButton closeBtn;

    // Color & size
    private Label colorLabel;
    private ColorPicker colorPicker;
    private Label sizeLabel;
    private Slider sizeSlider;

    // Zoom controls
    private ActionButton zoomOutBtn;
    private ActionButton zoomInBtn;
    private ActionButton zoomResetBtn;
    private Label zoomLabel;

    private final Map<String, KeyCode> hotkeys = new HashMap<>();

    // Keep reference to canvas for zoom callbacks
    private final DrawingCanvas canvas;

    public ToolBar(DrawingCanvas canvas, Stage stage) {
        this.canvas = canvas;
        setAlignment(Pos.TOP_LEFT);
        setPickOnBounds(false);
        setPadding(new Insets(15));

        initDefaultHotkeys();

        dragHandle = new Label("⣿⣿");
        rotateBtn = new ActionButton("🔄",
                "-fx-background-color: transparent; -fx-text-fill: #999999; -fx-font-size: 14px; -fx-cursor: hand; -fx-background-radius: 20;",
                "-fx-background-color: #333333; -fx-text-fill: white; -fx-font-size: 14px; -fx-cursor: hand; -fx-background-radius: 20;"
        );
        rotateBtn.setOnAction(e -> {
            isHorizontal = !isHorizontal;
            buildLayout();
        });

        toggleMode = new ToolButton("✏ Draw", null);
        toggleMode.setActive(true);   // เริ่มต้น = Draw mode (selected + active style)

        toggleMode.setOnAction(e -> {
            boolean isDraw = toggleMode.isSelected(); // true=Draw, false=View  (state AFTER click)
            Pane rootPane = (Pane) canvas.getParent();

            if (isDraw) {
                // ── Draw mode ─────────────────────────────────────────────────
                // background rgba(0.01) → pixel alpha > 0 → Win32 ส่ง mouse events มาที่ window
                // JavaFX hit-test กระจาย event ไปยัง canvas หรือ toolbar ตาม position
                toggleMode.setText("✏ Draw");
                canvas.setMouseTransparent(false);
                Main.setDrawMode(true);   // ensure WS_EX_TRANSPARENT ถูกลบออก
                if (rootPane != null)
                    rootPane.setStyle("-fx-background-color: rgba(255, 255, 255, 0.01);");
            } else {
                // ── View mode ─────────────────────────────────────────────────
                // background transparent (alpha=0) → Win32 pixel-alpha hit-test ล้มเหลว
                // → mouse events ผ่าน canvas ไปถึง desktop ด้านหลัง
                // Toolbar ยังคลิกได้เพราะมี background สีทึบ (alpha > 0)
                // *** ไม่ใช้ Main.setDrawMode(false) *** เพราะ WS_EX_TRANSPARENT จะทำให้
                // toolbar คลิกไม่ได้ด้วย (ใช้ได้เฉพาะ multi-stage เท่านั้น)
                toggleMode.setText("👁 View");
                canvas.setMouseTransparent(true);
                if (rootPane != null)
                    rootPane.setStyle("-fx-background-color: transparent;");
            }
        });

        // ── Drawing tools ─────────────────────────────────────────────────
        penBtn       = new ToolButton("Pen",       "pencil.png");
        highlightBtn = new ToolButton("Highlight", "highlighter.png");
        textBtn      = new ToolButton("Text",      "text_icon.png");
        eraserBtn    = new ToolButton("Eraser",    "eraser.png");

        penBtn.setOnAction(e -> { setActiveTool(penBtn); canvas.setPenMode(); });
        highlightBtn.setOnAction(e -> { setActiveTool(highlightBtn); canvas.setHighlightMode(); });
        textBtn.setOnAction(e -> { setActiveTool(textBtn); canvas.setTextMode(); });
        eraserBtn.setOnAction(e -> { setActiveTool(eraserBtn); canvas.setEraserMode(); });

        // ── Shape tools ───────────────────────────────────────────────────
        lineBtn     = new ToolButton("Line",     null);
        rectBtn     = new ToolButton("Rect",     null);
        circleBtn   = new ToolButton("Circle",   null);
        triangleBtn = new ToolButton("Triangle", null);

        // Use emoji text labels for shape buttons since there are no icons
        lineBtn.setText("╱");
        rectBtn.setText("▭");
        circleBtn.setText("○");
        triangleBtn.setText("△");

        lineBtn.setOnAction(e -> { setActiveTool(lineBtn); canvas.setLineMode(); });
        rectBtn.setOnAction(e -> { setActiveTool(rectBtn); canvas.setRectMode(); });
        circleBtn.setOnAction(e -> { setActiveTool(circleBtn); canvas.setCircleMode(); });
        triangleBtn.setOnAction(e -> { setActiveTool(triangleBtn); canvas.setTriangleMode(); });

        setActiveTool(penBtn);
        canvas.setPenMode();

        // ── Color & Size ─────────────────────────────────────────────────
        colorLabel = new Label("Color");
        sizeLabel  = new Label("Size: 4");

        colorPicker = new ColorPicker(Color.web("#E91E63"));
        colorPicker.setStyle("-fx-color-label-visible: false; -fx-background-color: #2b2b2b; -fx-background-radius: 20; -fx-cursor: hand;");
        colorPicker.setOnAction(e -> {
            canvas.setBrushColor(colorPicker.getValue());
            if (canvas.isEraser()) penBtn.fire();
        });
        canvas.setBrushColor(colorPicker.getValue());

        sizeSlider = new Slider(1, 100, 4);
        sizeSlider.setPrefWidth(80);
        sizeSlider.setStyle("-fx-cursor: hand;");
        sizeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            canvas.setBrushSize(newVal.doubleValue());
            sizeLabel.setText("Size: " + String.format("%.0f", newVal.doubleValue()));
        });

        // ── Action buttons ───────────────────────────────────────────────
        undoBtn = new ActionButton("↩ Undo");
        undoBtn.setOnAction(e -> canvas.undo());

        clearBtn = new ActionButton("🗑 Clear");
        clearBtn.setOnAction(e -> canvas.clearCanvas());

        saveBtn = new ActionButton("💾 Save");
        saveBtn.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Image As");
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            fileChooser.setInitialFileName("EasyPencil_" + timestamp + ".png");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG Files", "*.png"));
            File userDirectory = new File(System.getProperty("user.home"));
            if (userDirectory.exists()) fileChooser.setInitialDirectory(userDirectory);
            File file = fileChooser.showSaveDialog(stage);
            if (file != null) canvas.saveAsPng(file);
        });

        settingsBtn = new ActionButton("⚙");
        settingsBtn.setOnAction(e -> new HotkeySettings(this).showAndWait());

        closeBtn = new ActionButton("✕",
                "-fx-background-color: transparent; -fx-text-fill: #ff4d4d; -fx-background-radius: 20; -fx-font-size: 14px; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 6 12;",
                "-fx-background-color: #ff4d4d; -fx-text-fill: white; -fx-background-radius: 20; -fx-font-size: 14px; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 6 12;"
        );
        closeBtn.setOnAction(e -> javafx.application.Platform.exit());

        // ── Zoom controls ─────────────────────────────────────────────────
        zoomOutBtn   = new ActionButton("🔍-");
        zoomInBtn    = new ActionButton("🔍+");
        zoomResetBtn = new ActionButton("1:1");
        zoomLabel    = new Label("100%");

        zoomOutBtn.setOnAction(e -> { canvas.zoomOut(); updateZoomLabel(); });
        zoomInBtn.setOnAction(e -> { canvas.zoomIn(); updateZoomLabel(); });
        zoomResetBtn.setOnAction(e -> { canvas.resetZoom(); updateZoomLabel(); });

        buildLayout();
    }

    private void updateZoomLabel() {
        int pct = (int) Math.round(canvas.getZoomLevel() * 100);
        zoomLabel.setText(pct + "%");
    }

    public void toggleTheme() {
        this.isDarkMode = !isDarkMode;
        buildLayout();
    }

    public boolean isDarkMode() {
        return isDarkMode;
    }

    private void initDefaultHotkeys() {
        hotkeys.put("PEN",      KeyCode.P);
        hotkeys.put("HIGHLIGHT",KeyCode.H);
        hotkeys.put("TEXT",     KeyCode.T);
        hotkeys.put("ERASER",   KeyCode.E);
        hotkeys.put("LINE",     KeyCode.L);
        hotkeys.put("RECT",     KeyCode.R);
        hotkeys.put("CIRCLE",   KeyCode.C);
        hotkeys.put("TRIANGLE", KeyCode.G);
        hotkeys.put("UNDO",     KeyCode.Z);
        hotkeys.put("SAVE",     KeyCode.S);
    }

    public void setHotkey(String toolName, KeyCode newKey) {
        hotkeys.put(toolName.toUpperCase(), newKey);
    }

    public KeyCode getHotkey(String toolName) {
        return hotkeys.get(toolName.toUpperCase());
    }

    private void setActiveTool(ToolButton activeBtn) {
        ToolButton[] tools = {penBtn, highlightBtn, textBtn, eraserBtn,
                              lineBtn, rectBtn, circleBtn, triangleBtn};
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

        String bgColor     = isDarkMode ? "#1a1a1a" : "#ffffff";
        String borderColor = isDarkMode ? "#333333" : "#dddddd";
        String textColor   = isDarkMode ? "white"   : "#333333";
        String shadowColor = isDarkMode ? "rgba(0, 0, 0, 0.6)" : "rgba(0, 0, 0, 0.15)";

        container.setPadding(new Insets(8, 14, 8, 14));
        container.setStyle(
                "-fx-background-color: " + bgColor + ";"
                + "-fx-background-radius: 30;"
                + "-fx-border-color: " + borderColor + ";"
                + "-fx-border-radius: 30;"
                + "-fx-border-width: 1;"
                + "-fx-effect: dropshadow(gaussian, " + shadowColor + ", 12, 0, 0, 6);"
        );

        colorLabel.setTextFill(Color.web(textColor));
        colorLabel.setStyle("-fx-font-size: 11px;");
        sizeLabel.setTextFill(Color.web(textColor));
        sizeLabel.setStyle("-fx-font-size: 11px;");
        zoomLabel.setTextFill(Color.web(textColor));
        zoomLabel.setStyle("-fx-font-size: 11px; -fx-min-width: 36px; -fx-alignment: center;");
        dragHandle.setTextFill(Color.web(isDarkMode ? "#555555" : "#aaaaaa"));

        container.setOnMousePressed(e -> {
            xOffset = e.getSceneX() - this.getLayoutX();
            yOffset = e.getSceneY() - this.getLayoutY();
        });
        container.setOnMouseDragged(e -> {
            this.setLayoutX(e.getSceneX() - xOffset);
            this.setLayoutY(e.getSceneY() - yOffset);
        });

        container.getChildren().clear();
        container.getChildren().addAll(
                dragHandle, rotateBtn, getStyledSeparator(sepOrientation),
                toggleMode, getStyledSeparator(sepOrientation),
                // Drawing tools
                penBtn, highlightBtn, textBtn, eraserBtn, getStyledSeparator(sepOrientation),
                // Shape tools
                lineBtn, rectBtn, circleBtn, triangleBtn, getStyledSeparator(sepOrientation),
                // Color & size
                colorLabel, colorPicker, sizeLabel, sizeSlider, getStyledSeparator(sepOrientation),
                // Zoom controls
                zoomOutBtn, zoomLabel, zoomInBtn, zoomResetBtn, getStyledSeparator(sepOrientation),
                // Actions
                undoBtn, clearBtn, saveBtn, settingsBtn, getStyledSeparator(sepOrientation),
                closeBtn
        );

        this.getChildren().clear();
        this.getChildren().add(container);
    }

    private Separator getStyledSeparator(Orientation orientation) {
        Separator sep = new Separator(orientation);
        String sepColor = isDarkMode ? "#ffffff" : "#000000";
        sep.setStyle("-fx-opacity: 0.15; -fx-background-color: " + sepColor + ";");
        return sep;
    }

    public void setupShortcuts(Scene scene) {
        scene.setOnKeyPressed(e -> {
            if (e.getTarget() instanceof TextInputControl) return;
            if (!scene.getWindow().isFocused()) return;

            KeyCode code = e.getCode();

            // Tool hotkeys (no modifier)
            if (!e.isControlDown()) {
                if      (code == hotkeys.get("PEN"))      penBtn.fire();
                else if (code == hotkeys.get("HIGHLIGHT")) highlightBtn.fire();
                else if (code == hotkeys.get("TEXT"))      textBtn.fire();
                else if (code == hotkeys.get("ERASER"))    eraserBtn.fire();
                else if (code == hotkeys.get("LINE"))      lineBtn.fire();
                else if (code == hotkeys.get("RECT"))      rectBtn.fire();
                else if (code == hotkeys.get("CIRCLE"))    circleBtn.fire();
                else if (code == hotkeys.get("TRIANGLE"))  triangleBtn.fire();
            }

            // Ctrl shortcuts
            if (e.isControlDown()) {
                if      (code == hotkeys.get("UNDO"))  undoBtn.fire();
                else if (code == hotkeys.get("SAVE"))  saveBtn.fire();
                else if (code == KeyCode.EQUALS || code == KeyCode.ADD) {
                    canvas.zoomIn();
                    updateZoomLabel();
                } else if (code == KeyCode.MINUS || code == KeyCode.SUBTRACT) {
                    canvas.zoomOut();
                    updateZoomLabel();
                } else if (code == KeyCode.DIGIT0 || code == KeyCode.NUMPAD0) {
                    canvas.resetZoom();
                    updateZoomLabel();
                }
            }
        });

        // Ctrl+Scroll to zoom
        scene.setOnScroll(e -> {
            if (e.isControlDown()) {
                if (e.getDeltaY() > 0) canvas.zoomIn();
                else                   canvas.zoomOut();
                updateZoomLabel();
                e.consume();
            }
        });
    }
}
