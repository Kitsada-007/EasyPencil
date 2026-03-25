package com.easypencil.Widget;

import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class ToolButton extends ToggleButton {

    public ToolButton(String text, String iconFileName) {
        super(text);
        this.setStyle(normalStyle());
        loadIcon(iconFileName);
        
        this.setOnMouseEntered(e -> { if (!this.isSelected()) this.setStyle(hoverStyle()); });
        this.setOnMouseExited(e -> { if (!this.isSelected()) this.setStyle(normalStyle()); });
    }

    public void setActive(boolean isActive) {
        this.setSelected(isActive);
        this.setStyle(isActive ? activeStyle() : normalStyle());
    }

    private void loadIcon(String fileName) {
        if (fileName == null) return;
        try {
            Image img = new Image("file:app/src/main/resources/asset/" + fileName);
            ImageView view = new ImageView(img);
            view.setFitWidth(16); view.setFitHeight(16);
            this.setGraphic(view);
        } catch (Exception e) {}
    }

    private String normalStyle() { return "-fx-background-color: transparent; -fx-text-fill: #888; -fx-cursor: hand; -fx-padding: 6 12; -fx-background-radius: 20;"; }
    private String hoverStyle() { return "-fx-background-color: rgba(128,128,128,0.2); -fx-text-fill: #E91E63; -fx-cursor: hand; -fx-padding: 6 12; -fx-background-radius: 20;"; }
    private String activeStyle() { return "-fx-background-color: #E91E63; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 6 12; -fx-background-radius: 20;"; }
}