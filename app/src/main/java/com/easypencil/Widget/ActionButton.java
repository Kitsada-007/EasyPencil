package com.easypencil.Widget;

import javafx.scene.control.Button;

public class ActionButton extends Button {
    private String customNormal, customHover;

    public ActionButton(String text) {
        super(text);
        this.customNormal = "-fx-background-color: transparent; -fx-text-fill: #888; -fx-cursor: hand; -fx-padding: 6 12; -fx-background-radius: 20;";
        this.customHover = "-fx-background-color: rgba(128,128,128,0.2); -fx-text-fill: white; -fx-cursor: hand; -fx-padding: 6 12; -fx-background-radius: 20;";
        setup();
    }

    public ActionButton(String text, String normal, String hover) {
        super(text);
        this.customNormal = normal;
        this.customHover = hover;
        setup();
    }

    private void setup() {
        this.setStyle(customNormal);
        this.setOnMouseEntered(e -> this.setStyle(customHover));
        this.setOnMouseExited(e -> this.setStyle(customNormal));
    }
}