package com.easypencil;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class ToolBar extends VBox {

    public ToolBar(DrawingCanvas canvas, Stage stage) {
        setAlignment(Pos.TOP_LEFT);
        setPickOnBounds(false);
        setPadding(new Insets(10));

        HBox toolbar = new HBox(8);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.setPadding(new Insets(8, 12, 8, 12));
        toolbar.setStyle(
                "-fx-background-color: rgba(30,30,30,0.85);"
                + "-fx-background-radius: 12;"
        );

        // Toggle Draw/View
        ToggleButton toggleMode = new ToggleButton("✏ Draw");
        toggleMode.setSelected(true);
        toggleMode.setStyle(selectedStyle());
        toggleMode.setOnAction(e -> {
            if (toggleMode.isSelected()) {
                toggleMode.setText("✏ Draw");
                toggleMode.setStyle(selectedStyle());
                canvas.setMouseTransparent(false);
                Main.setDrawMode(true);
            } else {
                toggleMode.setText("👁 View");
                toggleMode.setStyle(normalStyle());
                canvas.setMouseTransparent(true);
                Main.setDrawMode(false);
            }
        });

        // Color picker
        ColorPicker colorPicker = new ColorPicker(Color.RED);
        colorPicker.setOnAction(e -> {
            canvas.setBrushColor(colorPicker.getValue());
            canvas.setEraser(false);
        });

        // Size slider
        Slider sizeSlider = new Slider(1, 30, 4);
        sizeSlider.setPrefWidth(80);
        sizeSlider.valueProperty().addListener((obs, oldVal, newVal)
                -> canvas.setBrushSize(newVal.doubleValue())
        );

        // Eraser
        ToggleButton eraserBtn = new ToggleButton("⬜ Eraser");
        eraserBtn.setStyle(normalStyle());
        eraserBtn.setOnAction(e -> {
            canvas.setEraser(eraserBtn.isSelected());
            eraserBtn.setStyle(eraserBtn.isSelected() ? selectedStyle() : normalStyle());
        });

        // Undo
        Button undoBtn = new Button("↩ Undo");
        undoBtn.setStyle(normalStyle());
        undoBtn.setOnAction(e -> canvas.undo());

        // Clear
        Button clearBtn = new Button("🗑 Clear");
        clearBtn.setStyle(normalStyle());
        clearBtn.setOnAction(e -> canvas.clearCanvas());

        // Close
        Button closeBtn = new Button("✕");
        closeBtn.setStyle(
                "-fx-background-color: #c0392b; -fx-text-fill: white;"
                + "-fx-background-radius: 6; -fx-font-size: 13px;"
        );
        closeBtn.setOnAction(e -> stage.close());

        toolbar.getChildren().addAll(
                toggleMode,
                new Separator(javafx.geometry.Orientation.VERTICAL),
                new Label("Color:"), colorPicker,
                new Label("Size:"), sizeSlider,
                new Separator(javafx.geometry.Orientation.VERTICAL),
                eraserBtn, undoBtn, clearBtn,
                new Separator(javafx.geometry.Orientation.VERTICAL),
                closeBtn
        );

        getChildren().add(toolbar);
    }

    private String selectedStyle() {
        return "-fx-background-color: #3498db; -fx-text-fill: white;"
                + "-fx-background-radius: 6; -fx-font-size: 13px;";
    }

    private String normalStyle() {
        return "-fx-background-color: #555; -fx-text-fill: white;"
                + "-fx-background-radius: 6; -fx-font-size: 13px;";
    }
}
