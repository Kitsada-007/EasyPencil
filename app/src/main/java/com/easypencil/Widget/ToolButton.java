package com.easypencil.Widget;

import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class ToolButton extends ToggleButton {

    public ToolButton(String text, String iconFileName) {
        super(text);
        this.setStyle(normalStyle());
        loadIcon(iconFileName);

        this.setOnMouseEntered(e -> {
            if (!this.isSelected()) {
                this.setStyle(hoverStyle());
        
            }});
        this.setOnMouseExited(e -> {
            if (!this.isSelected()) {
                this.setStyle(normalStyle());
        
            }});
    }

    public void setActive(boolean isActive) {
        this.setSelected(isActive);
        this.setStyle(isActive ? activeStyle() : normalStyle());
    }

    private void loadIcon(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return;
        }
        try {
            java.net.URL resourceUrl = getClass().getResource("/asset/" + fileName);
            if (resourceUrl == null) {
                System.out.println("❌ ไม่พบไอคอน: " + fileName);
                return;
            }
            Image img = new Image(resourceUrl.toExternalForm());
            ImageView view = new ImageView(img);
            view.setFitWidth(16);
            view.setFitHeight(16);
            view.setPreserveRatio(true);
            this.setGraphic(view);

        } catch (Exception e) {
            System.out.println("❌ เกิดข้อผิดพลาดตอนโหลดไอคอน " + fileName + ": " + e.getMessage());
        }
    }

    // --- เก็บ CSS ไว้ใน Widget นี้เลย ---
    private String normalStyle() {
        return "-fx-background-color: transparent; -fx-text-fill: #cccccc;"
                + "-fx-background-radius: 20; -fx-font-size: 12px; -fx-cursor: hand; -fx-padding: 6 12;";
    }

    private String hoverStyle() {
        return "-fx-background-color: #333333; -fx-text-fill: #ffffff;"
                + "-fx-background-radius: 20; -fx-font-size: 12px; -fx-cursor: hand; -fx-padding: 6 12;";
    }

    private String activeStyle() {
        return "-fx-background-color: #E91E63; -fx-text-fill: #ffffff;"
                + "-fx-background-radius: 20; -fx-font-size: 12px; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 6 12;"
                + "-fx-effect: dropshadow(gaussian, rgba(233, 30, 99, 0.4), 8, 0, 0, 0);";
    }
}
