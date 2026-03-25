package com.easypencil;

import java.io.File;
import java.util.ArrayDeque;
import java.util.Deque;

import com.easypencil.Widget.FloatingTextBox;
import com.easypencil.util.ScreenCaptureUtil;

import javafx.scene.Cursor;
import javafx.scene.ImageCursor;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;

public class DrawingCanvas extends Pane {

    private final Canvas canvas;
    private final GraphicsContext gc;
    private final Deque<WritableImage> undoStack = new ArrayDeque<>();

    private Color brushColor = Color.RED;
    private double brushSize = 4.0;

    private boolean eraser = false;
    private boolean textMode = false;
    private boolean highlightMode = false;

    private FloatingTextBox activeTextBox = null;

    private Cursor penCursor = Cursor.DEFAULT;
    private Cursor highlightCursor = Cursor.DEFAULT;
    private Cursor eraserCursor = Cursor.DEFAULT;
    private Cursor textCursor = Cursor.TEXT;

    public DrawingCanvas() {
        double w = javafx.stage.Screen.getPrimary().getBounds().getWidth();
        double h = javafx.stage.Screen.getPrimary().getBounds().getHeight();

        canvas = new Canvas(w, h);
        gc = canvas.getGraphicsContext2D();
        gc.setLineJoin(StrokeLineJoin.ROUND);

        setPickOnBounds(false);
        canvas.setPickOnBounds(true);
        canvas.setMouseTransparent(false);

        getChildren().add(canvas);

        loadCursors();
        setupMouseEvents();
    }

    private void loadCursors() {
        try {
            String penUrl = getClass().getResource("/asset/pencil.png").toExternalForm();
            Image penImg = new Image(penUrl);
            penCursor = new ImageCursor(penImg, 0, 512);

            String highlightUrl = getClass().getResource("/asset/highlighter.png").toExternalForm();
            Image highlightImg = new Image(highlightUrl);
            highlightCursor = new ImageCursor(highlightImg, 0, 512);

            String textUrl = getClass().getResource("/asset/text_icon.png").toExternalForm();
            Image textImg = new Image(textUrl);
            textCursor = new ImageCursor(textImg, 0, 0);

            String eraserUrl = getClass().getResource("/asset/eraser.png").toExternalForm();
            Image eraserImg = new Image(eraserUrl);
            eraserCursor = new ImageCursor(eraserImg, eraserImg.getWidth() / 2, eraserImg.getHeight() / 2);

        } catch (Exception e) {
            System.err.println("ไม่สามารถโหลดไฟล์เคอร์เซอร์ได้: " + e.getMessage());

            penCursor = Cursor.CROSSHAIR;
            highlightCursor = Cursor.CROSSHAIR;
            textCursor = Cursor.TEXT;
            eraserCursor = Cursor.CLOSED_HAND;
        }
    }

    private void setupMouseEvents() {
        canvas.setOnMousePressed(e -> {
            if (textMode) {
                if (activeTextBox != null) {
                    finalizeText();
                    return;
                }
                activeTextBox = new FloatingTextBox(e.getX(), e.getY(), brushColor, brushSize, this::finalizeText);
                this.getChildren().add(activeTextBox);
                activeTextBox.focusText();
                return;
            }

            saveSnapshot();
            gc.setStroke(brushColor);

            if (highlightMode) {
                gc.setGlobalAlpha(0.4);
                gc.setLineWidth(brushSize * 3);
                gc.setLineCap(StrokeLineCap.ROUND);
            } else {
                gc.setGlobalAlpha(1.0);
                gc.setLineWidth(brushSize);
                gc.setLineCap(StrokeLineCap.ROUND);
            }

            gc.beginPath();
            gc.moveTo(e.getX(), e.getY());
            gc.stroke();
        });

        canvas.setOnMouseDragged(e -> {
            if (textMode) {
                return;
            }

            if (eraser) {
                gc.setGlobalAlpha(1.0);
                gc.clearRect(e.getX() - brushSize * 2, e.getY() - brushSize * 2, brushSize * 4, brushSize * 4);
            } else {
                gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
                gc.setGlobalAlpha(1.0);
                gc.drawImage(undoStack.peek(), 0, 0);

                if (highlightMode) {
                    gc.setGlobalAlpha(0.4);
                }

                gc.lineTo(e.getX(), e.getY());
                gc.stroke();
            }
        });

        canvas.setOnMouseReleased(e -> {
            if (!textMode) {
                gc.closePath();
            }
        });
    }

    private void saveSnapshot() {
        SnapshotParameters params = new SnapshotParameters();
        params.setFill(Color.TRANSPARENT);
        WritableImage snapshot = canvas.snapshot(params, null);
        undoStack.push(snapshot);
        if (undoStack.size() > 30) {
            undoStack.pollLast();
        }
    }

    private void finalizeText() {
        if (activeTextBox != null && !activeTextBox.getText().trim().isEmpty()) {
            saveSnapshot();
            gc.setGlobalAlpha(1.0);
            gc.setFill(brushColor);
            double fontSize = Math.max(16, brushSize * 4);
            gc.setFont(javafx.scene.text.Font.font("System", javafx.scene.text.FontWeight.BOLD, fontSize));

            gc.fillText(activeTextBox.getText(), activeTextBox.getStampX(), activeTextBox.getStampY());
        }

        if (activeTextBox != null) {
            this.getChildren().remove(activeTextBox);
            activeTextBox = null;
        }
    }

    public void setPenMode() {
        this.textMode = false;
        this.eraser = false;
        this.highlightMode = false;
        this.setCursor(penCursor);
        if (activeTextBox != null) {
            finalizeText();
        }
    }

    public void setHighlightMode() {
        this.textMode = false;
        this.eraser = false;
        this.highlightMode = true;
        this.setCursor(highlightCursor);
        if (activeTextBox != null) {
            finalizeText();
        }
    }

    public void setTextMode() {
        this.textMode = true;
        this.eraser = false;
        this.highlightMode = false;
        this.setCursor(textCursor);
    }

    public void setEraserMode() {
        this.textMode = false;
        this.eraser = true;
        this.highlightMode = false;
        this.setCursor(eraserCursor);
        if (activeTextBox != null) {
            finalizeText();
        }
    }

    public void undo() {
        if (!undoStack.isEmpty()) {
            gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
            gc.setGlobalAlpha(1.0);
            gc.drawImage(undoStack.pop(), 0, 0);
        }
    }

    public void clearCanvas() {
        saveSnapshot();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }

    public void setBrushColor(Color color) {
        this.brushColor = color;
    }

    public void setBrushSize(double size) {
        this.brushSize = size;
    }

    public boolean isEraser() {
        return eraser;
    }

    public void saveAsPng(File file) {
        ScreenCaptureUtil.saveScreenAsPng(file, this::finalizeText);
    }
}
