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
        if (toolbar.getScene() != null) {
            this.initOwner(toolbar.getScene().getWindow());
        }
        this.setAlwaysOnTop(true);
        this.initModality(Modality.APPLICATION_MODAL);
        this.initStyle(StageStyle.UTILITY);
        this.setTitle("Settings");

        boolean isDark = toolbar.isDarkMode();
        String bgColor = isDark ? "#1a1a1a" : "#ffffff";
        String textColor = isDark ? "white" : "#333333";
        String btnBase = isDark ? "#333333" : "#eeeeee";

        VBox root = new VBox(20);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: " + bgColor + "; -fx-border-color: #444; -fx-border-width: 1;");

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(12);
        grid.setAlignment(Pos.CENTER);

        Label themeLabel = new Label("Theme:");
        themeLabel.setStyle("-fx-text-fill: " + textColor + "; -fx-font-weight: bold;");

        Button themeBtn = new Button(isDark ? "🌙 Dark Mode" : "☀️ Light Mode");
        themeBtn.setStyle("-fx-background-color: " + btnBase + "; -fx-text-fill: " + textColor + "; -fx-min-width: 120; -fx-cursor: hand;");

        themeBtn.setOnAction(e -> {
            toolbar.toggleTheme();
            this.close();
            new HotkeySettings(toolbar).show();
        });

        grid.add(themeLabel, 0, 0);
        grid.add(themeBtn, 1, 0);

        String[] tools = {"PEN", "HIGHLIGHT", "TEXT", "ERASER", "UNDO", "SAVE"};
        int row = 1;

        for (String tool : tools) {
            Label nameLabel = new Label(tool + "\t:\tCTRL +");
            nameLabel.setStyle("-fx-text-fill: " + textColor + ";");

            Button keyBtn = new Button(toolbar.getHotkey(tool).toString());
            keyBtn.setStyle("-fx-background-color: " + btnBase + "; -fx-text-fill: #E91E63; -fx-min-width: 120; -fx-font-weight: bold; -fx-cursor: hand;");

            keyBtn.setOnAction(e -> {
                keyBtn.setText("Press Key...");
                keyBtn.setStyle("-fx-background-color: #E91E63; -fx-text-fill: white; -fx-min-width: 120;");

                keyBtn.setOnKeyPressed(keyEvent -> {
                    KeyCode newCode = keyEvent.getCode();
                    toolbar.setHotkey(tool, newCode);
                    keyBtn.setText(newCode.toString());
                    keyBtn.setStyle("-fx-background-color: " + btnBase + "; -fx-text-fill: #E91E63; -fx-min-width: 120;");

                    keyEvent.consume();
                    keyBtn.setOnKeyPressed(null);
                });
            });

            grid.add(nameLabel, 0, row);
            grid.add(keyBtn, 1, row);
            row++;
        }

        Button doneBtn = new Button("Save & Close");
        doneBtn.setStyle("-fx-background-color: #E91E63; -fx-text-fill: white; -fx-padding: 8 25; -fx-background-radius: 20; -fx-cursor: hand;");
        doneBtn.setOnAction(e -> {
            if (toolbar.getScene() != null) {
                toolbar.getScene().getRoot().requestFocus();
            }
            this.close();
        });

        root.getChildren().addAll(grid, doneBtn);

        Scene scene = new Scene(root);
        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
                doneBtn.fire();
            }
        });

        this.setScene(scene);
    }
}
