package com.easypencil;

import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayDeque;
import java.util.Deque;
import javax.imageio.ImageIO;

import com.easypencil.Widget.FloatingTextBox;
import com.easypencil.util.ScreenCaptureUtil;

import javafx.application.Platform;
import javafx.scene.Cursor;
import javafx.scene.ImageCursor;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import javafx.stage.Stage;

public class DrawingCanvas extends Pane {

    public enum DrawMode {
        PEN, HIGHLIGHT, ERASER, TEXT,
        SHAPE_LINE, SHAPE_RECT, SHAPE_CIRCLE, SHAPE_TRIANGLE
    }

    private final Canvas canvas;
    private final GraphicsContext gc;
    private final Deque<WritableImage> undoStack = new ArrayDeque<>();

    private Color brushColor = Color.RED;
    private double brushSize = 4.0;

    private DrawMode drawMode = DrawMode.PEN;
    private FloatingTextBox activeTextBox = null;

    private Cursor penCursor     = Cursor.DEFAULT;
    private Cursor highlightCursor = Cursor.DEFAULT;
    private Cursor eraserCursor  = Cursor.DEFAULT;
    private Cursor textCursor    = Cursor.TEXT;

    // Start point สำหรับ shape / straight-line
    private double startX, startY;

    // ── Screen-capture zoom ──────────────────────────────────────────────────
    // zoom ซูมหน้าจอจริง (ไม่ใช่ซูม canvas) — แสดง screenshot crop เป็น background
    private double zoomLevel   = 1.0;
    private ImageView zoomBgView;           // ImageView สำหรับพื้นหลังที่ถูก zoom
    private volatile boolean isCapturing = false;

    private final double screenW;
    private final double screenH;

    public DrawingCanvas() {
        screenW = javafx.stage.Screen.getPrimary().getBounds().getWidth();
        screenH = javafx.stage.Screen.getPrimary().getBounds().getHeight();

        canvas = new Canvas(screenW, screenH);
        gc = canvas.getGraphicsContext2D();
        gc.setLineJoin(StrokeLineJoin.ROUND);

        setPickOnBounds(false);
        canvas.setPickOnBounds(true);
        canvas.setMouseTransparent(false);

        getChildren().add(canvas);   // canvas เป็น child แรก (zoomBgView จะ insert ที่ index 0)

        loadCursors();
        setupMouseEvents();
    }

    // ── Cursors ──────────────────────────────────────────────────────────────

    private void loadCursors() {
        try {
            penCursor       = new ImageCursor(new Image(getClass().getResource("/asset/pencil.png").toExternalForm()), 0, 512);
            highlightCursor = new ImageCursor(new Image(getClass().getResource("/asset/highlighter.png").toExternalForm()), 0, 512);
            textCursor      = new ImageCursor(new Image(getClass().getResource("/asset/text_icon.png").toExternalForm()), 0, 0);
            Image eraserImg = new Image(getClass().getResource("/asset/eraser.png").toExternalForm());
            eraserCursor    = new ImageCursor(eraserImg, eraserImg.getWidth() / 2, eraserImg.getHeight() / 2);
        } catch (Exception e) {
            System.err.println("ไม่สามารถโหลดเคอร์เซอร์: " + e.getMessage());
            penCursor = highlightCursor = Cursor.CROSSHAIR;
            textCursor = Cursor.TEXT;
            eraserCursor = Cursor.CLOSED_HAND;
        }
    }

    // ── Mouse events ──────────────────────────────────────────────────────────

    private void setupMouseEvents() {
        canvas.setOnMousePressed(e -> {
            if (drawMode == DrawMode.TEXT) {
                if (activeTextBox != null) { finalizeText(); return; }
                activeTextBox = new FloatingTextBox(e.getX(), e.getY(), brushColor, brushSize, this::finalizeText);
                getChildren().add(activeTextBox);
                activeTextBox.focusText();
                return;
            }

            saveSnapshot();
            startX = e.getX();
            startY = e.getY();

            gc.setStroke(brushColor);
            gc.setFill(brushColor);
            gc.setLineCap(StrokeLineCap.ROUND);

            if (drawMode == DrawMode.HIGHLIGHT) {
                gc.setGlobalAlpha(0.4);
                gc.setLineWidth(brushSize * 3);
            } else {
                gc.setGlobalAlpha(1.0);
                gc.setLineWidth(brushSize);
            }

            if (drawMode == DrawMode.PEN || drawMode == DrawMode.HIGHLIGHT) {
                gc.beginPath();
                gc.moveTo(startX, startY);
                gc.stroke();
            }
        });

        canvas.setOnMouseDragged(e -> {
            if (drawMode == DrawMode.TEXT) return;
            double curX = e.getX();
            double curY = e.getY();

            switch (drawMode) {
                case ERASER -> {
                    gc.setGlobalAlpha(1.0);
                    gc.clearRect(curX - brushSize * 2, curY - brushSize * 2, brushSize * 4, brushSize * 4);
                }
                case PEN -> {
                    restoreSnapshot();
                    gc.setGlobalAlpha(1.0);
                    gc.setLineWidth(brushSize);
                    gc.setLineCap(StrokeLineCap.ROUND);
                    gc.setStroke(brushColor);
                    if (e.isShiftDown()) {
                        double[] s = snapToAxis(startX, startY, curX, curY);
                        gc.strokeLine(startX, startY, s[0], s[1]);
                        gc.beginPath(); gc.moveTo(startX, startY);
                    } else {
                        gc.lineTo(curX, curY);
                        gc.stroke();
                    }
                }
                case HIGHLIGHT -> {
                    restoreSnapshot();
                    gc.setGlobalAlpha(0.4);
                    gc.setLineWidth(brushSize * 3);
                    gc.setLineCap(StrokeLineCap.ROUND);
                    gc.setStroke(brushColor);
                    if (e.isShiftDown()) {
                        double[] s = snapToAxis(startX, startY, curX, curY);
                        gc.strokeLine(startX, startY, s[0], s[1]);
                        gc.beginPath(); gc.moveTo(startX, startY);
                    } else {
                        gc.lineTo(curX, curY); gc.stroke();
                    }
                }
                case SHAPE_LINE -> {
                    restoreSnapshot();
                    gc.setGlobalAlpha(1.0);
                    gc.setLineWidth(brushSize);
                    gc.setLineCap(StrokeLineCap.ROUND);
                    gc.setStroke(brushColor);
                    double[] end = e.isShiftDown() ? snapToAxis(startX, startY, curX, curY) : new double[]{curX, curY};
                    gc.strokeLine(startX, startY, end[0], end[1]);
                }
                case SHAPE_RECT -> {
                    restoreSnapshot();
                    gc.setGlobalAlpha(1.0);
                    gc.setLineWidth(brushSize);
                    gc.setStroke(brushColor);
                    double rx = Math.min(startX, curX), ry = Math.min(startY, curY);
                    double rw = Math.abs(curX - startX), rh = Math.abs(curY - startY);
                    if (e.isShiftDown()) {
                        double side = Math.min(rw, rh);
                        rx = startX < curX ? startX : startX - side;
                        ry = startY < curY ? startY : startY - side;
                        rw = rh = side;
                    }
                    gc.strokeRect(rx, ry, rw, rh);
                }
                case SHAPE_CIRCLE -> {
                    restoreSnapshot();
                    gc.setGlobalAlpha(1.0);
                    gc.setLineWidth(brushSize);
                    gc.setStroke(brushColor);
                    double ox = Math.min(startX, curX), oy = Math.min(startY, curY);
                    double ow = Math.abs(curX - startX), oh = Math.abs(curY - startY);
                    if (e.isShiftDown()) {
                        double side = Math.min(ow, oh);
                        ox = startX < curX ? startX : startX - side;
                        oy = startY < curY ? startY : startY - side;
                        ow = oh = side;
                    }
                    gc.strokeOval(ox, oy, ow, oh);
                }
                case SHAPE_TRIANGLE -> {
                    restoreSnapshot();
                    gc.setGlobalAlpha(1.0);
                    gc.setLineWidth(brushSize);
                    gc.setLineCap(StrokeLineCap.ROUND);
                    gc.setStroke(brushColor);
                    double cx = (startX + curX) / 2.0;
                    gc.strokePolygon(new double[]{cx, startX, curX}, new double[]{startY, curY, curY}, 3);
                }
            }
        });

        canvas.setOnMouseReleased(e -> {
            if (drawMode == DrawMode.PEN || drawMode == DrawMode.HIGHLIGHT)
                gc.closePath();
        });
    }

    private void restoreSnapshot() {
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        gc.setGlobalAlpha(1.0);
        if (!undoStack.isEmpty()) gc.drawImage(undoStack.peek(), 0, 0);
    }

    private double[] snapToAxis(double x1, double y1, double x2, double y2) {
        double dx = Math.abs(x2 - x1), dy = Math.abs(y2 - y1);
        if (dx > dy * 2)      return new double[]{x2, y1};   // แนวนอน
        else if (dy > dx * 2) return new double[]{x1, y2};   // แนวตั้ง
        else {                                                 // 45°
            double len = Math.min(dx, dy);
            return new double[]{x1 + len * Math.signum(x2 - x1), y1 + len * Math.signum(y2 - y1)};
        }
    }

    private void saveSnapshot() {
        SnapshotParameters p = new SnapshotParameters();
        p.setFill(Color.TRANSPARENT);
        undoStack.push(canvas.snapshot(p, null));
        if (undoStack.size() > 30) undoStack.pollLast();
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
        if (activeTextBox != null) { getChildren().remove(activeTextBox); activeTextBox = null; }
    }

    // ── Mode setters ─────────────────────────────────────────────────────────

    public void setPenMode()      { drawMode = DrawMode.PEN;           setCursor(penCursor);       finalizeIfText(); }
    public void setHighlightMode(){ drawMode = DrawMode.HIGHLIGHT;     setCursor(highlightCursor); finalizeIfText(); }
    public void setTextMode()     { drawMode = DrawMode.TEXT;          setCursor(textCursor); }
    public void setEraserMode()   { drawMode = DrawMode.ERASER;        setCursor(eraserCursor);    finalizeIfText(); }
    public void setLineMode()     { drawMode = DrawMode.SHAPE_LINE;    setCursor(Cursor.CROSSHAIR); finalizeIfText(); }
    public void setRectMode()     { drawMode = DrawMode.SHAPE_RECT;    setCursor(Cursor.CROSSHAIR); finalizeIfText(); }
    public void setCircleMode()   { drawMode = DrawMode.SHAPE_CIRCLE;  setCursor(Cursor.CROSSHAIR); finalizeIfText(); }
    public void setTriangleMode() { drawMode = DrawMode.SHAPE_TRIANGLE;setCursor(Cursor.CROSSHAIR); finalizeIfText(); }
    private void finalizeIfText() { if (activeTextBox != null) finalizeText(); }

    // ── Screen-capture zoom ──────────────────────────────────────────────────
    //
    // แทนที่จะ scale canvas (ซึ่งจะซูมแค่สิ่งที่วาด),
    // เราซ่อน canvas stage → ถ่ายภาพหน้าจอจริง → crop บริเวณกลาง → แสดงเป็น background ImageView
    // ผลลัพธ์: หน้าจอจริงถูก zoom แต่เส้นวาดยังอยู่ที่พิกัด 1:1 บนหน้าจอ

    public void zoomIn() {
        if (isCapturing) return;
        zoomLevel = Math.min(zoomLevel * 1.25, 4.0);
        captureZoomedScreen();
    }

    public void zoomOut() {
        if (isCapturing) return;
        zoomLevel = zoomLevel / 1.25;
        if (zoomLevel <= 1.05) {   // floating-point safety margin
            zoomLevel = 1.0;
            removeZoomBackground();
        } else {
            captureZoomedScreen();
        }
    }

    public void resetZoom() {
        zoomLevel = 1.0;
        removeZoomBackground();
    }

    public double getZoomLevel() { return zoomLevel; }

    private void removeZoomBackground() {
        if (zoomBgView != null) {
            getChildren().remove(zoomBgView);
            zoomBgView = null;
        }
    }

    private void captureZoomedScreen() {
        if (getScene() == null) return;
        isCapturing = true;

        // ซ่อนแค่ canvas node + ทำ parent background โปร่งใส
        // ไม่ต้อง stage.hide() ซึ่งจะซ่อน toolbar ด้วยและทำให้ scene state ผิดพลาด
        Pane parent = (Pane) getParent();
        String savedStyle = (parent != null) ? parent.getStyle() : "";
        canvas.setVisible(false);
        if (parent != null) parent.setStyle("-fx-background-color: transparent;");

        // รอ 2 JavaFX pulse ให้ render อัปเดต จากนั้น background thread ถ่ายภาพ
        Platform.runLater(() -> Platform.runLater(() -> new Thread(() -> {
            try {
                Thread.sleep(50);   // รอ OS compositor flush

                int iW = (int) screenW;
                int iH = (int) screenH;
                int rw = (int)(iW / zoomLevel);
                int rh = (int)(iH / zoomLevel);
                int rx = (iW - rw) / 2;
                int ry = (iH - rh) / 2;

                Robot robot = new Robot();
                BufferedImage full    = robot.createScreenCapture(new Rectangle(iW, iH));
                BufferedImage cropped = full.getSubimage(rx, ry, rw, rh);

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(cropped, "png", baos);
                byte[] data = baos.toByteArray();

                Platform.runLater(() -> {
                    // คืนค่า canvas และ background
                    canvas.setVisible(true);
                    if (parent != null) parent.setStyle(savedStyle);

                    Image fxImg = new Image(new ByteArrayInputStream(data));
                    if (zoomBgView == null) {
                        zoomBgView = new ImageView(fxImg);
                        zoomBgView.setFitWidth(screenW);
                        zoomBgView.setFitHeight(screenH);
                        zoomBgView.setPreserveRatio(false);
                        zoomBgView.setSmooth(true);
                        getChildren().add(0, zoomBgView);
                    } else {
                        zoomBgView.setImage(fxImg);
                    }
                    isCapturing = false;
                });

            } catch (Exception ex) {
                System.err.println("Screen capture error: " + ex.getMessage());
                Platform.runLater(() -> {
                    canvas.setVisible(true);
                    if (parent != null) parent.setStyle(savedStyle);
                    isCapturing = false;
                });
            }
        }).start()));
    }

    // ── Actions ──────────────────────────────────────────────────────────────

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

    public void setBrushColor(Color color) { this.brushColor = color; }
    public void setBrushSize(double size)  { this.brushSize  = size;  }
    public boolean isEraser()              { return drawMode == DrawMode.ERASER; }

    public void saveAsPng(File file) {
        ScreenCaptureUtil.saveScreenAsPng(file, this::finalizeText);
    }
}
