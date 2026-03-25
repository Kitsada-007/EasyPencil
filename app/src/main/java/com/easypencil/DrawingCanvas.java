package com.easypencil;

import java.util.ArrayList;
import java.util.List;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;

public class DrawingCanvas extends Pane {

    private final Canvas canvas;
    private final GraphicsContext gc;

    private final List<DrawAction> history = new ArrayList<>();
    private DrawAction currentAction;

    private Color brushColor = Color.RED;
    private double brushSize = 4.0;
    private boolean eraser = false;

    public DrawingCanvas() {
        double w = javafx.stage.Screen.getPrimary().getBounds().getWidth();
        double h = javafx.stage.Screen.getPrimary().getBounds().getHeight();

        canvas = new Canvas(w, h);
        gc = canvas.getGraphicsContext2D();
        gc.setLineCap(StrokeLineCap.ROUND);
        gc.setLineJoin(StrokeLineJoin.ROUND);

        setPickOnBounds(false);
        canvas.setPickOnBounds(true);
        canvas.setMouseTransparent(false);

        getChildren().add(canvas);
        setupMouseEvents();
    }

    private void setupMouseEvents() {
        canvas.setOnMousePressed(e -> {
            currentAction = new DrawAction(brushColor, brushSize, eraser);
            currentAction.addPoint(e.getX(), e.getY());
            history.add(currentAction);

            if (eraser) {
                // ลบจุดแรกที่คลิก
                double size = brushSize * 4;
                gc.clearRect(e.getX() - size / 2, e.getY() - size / 2, size, size);
            } else {
                gc.setStroke(brushColor);
                gc.setLineWidth(brushSize);
                gc.beginPath();
                gc.moveTo(e.getX(), e.getY());
                gc.stroke();
            }
        });

        canvas.setOnMouseDragged(e -> {
            if (currentAction != null) {
                currentAction.addPoint(e.getX(), e.getY());
            }

            if (eraser) {
                // อัลกอริทึมถมช่องว่างของยางลบ: ทำให้เวลาลากเมาส์เร็วๆ รอยลบก็ยังเชื่อมต่อกันเนียนๆ
                int lastIdx = currentAction.xPoints.size() - 2;
                if (lastIdx >= 0) {
                    double lastX = currentAction.xPoints.get(lastIdx);
                    double lastY = currentAction.yPoints.get(lastIdx);
                    double currentX = e.getX();
                    double currentY = e.getY();
                    double size = brushSize * 4;

                    double distance = Math.hypot(currentX - lastX, currentY - lastY);
                    int steps = Math.max(1, (int) distance);
                    for (int i = 0; i <= steps; i++) {
                        double interpX = lastX + (currentX - lastX) * ((double) i / steps);
                        double interpY = lastY + (currentY - lastY) * ((double) i / steps);
                        gc.clearRect(interpX - size / 2, interpY - size / 2, size, size);
                    }
                }
            } else {
                gc.lineTo(e.getX(), e.getY());
                gc.stroke();
            }
        });

        canvas.setOnMouseReleased(e -> {
            if (!eraser) {
                gc.closePath();
            }
        });
    }

    public void undo() {
        if (!history.isEmpty()) {
            history.remove(history.size() - 1);
            redrawAll();
        }
    }

    public void clearCanvas() {
        history.clear();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }

    // วาดใหม่ทั้งหมดเพื่อทำ Undo (รองรับทั้งเส้นวาดยางลบ)
    private void redrawAll() {
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        for (DrawAction action : history) {
            if (action.xPoints.isEmpty()) {
                continue;
            }

            if (action.eraser) {
                double size = action.size * 4;
                for (int j = 0; j < action.xPoints.size(); j++) {
                    double x = action.xPoints.get(j);
                    double y = action.yPoints.get(j);

                    if (j > 0) {
                        double lastX = action.xPoints.get(j - 1);
                        double lastY = action.yPoints.get(j - 1);
                        double distance = Math.hypot(x - lastX, y - lastY);
                        int steps = Math.max(1, (int) distance);
                        for (int i = 0; i <= steps; i++) {
                            double interpX = lastX + (x - lastX) * ((double) i / steps);
                            double interpY = lastY + (y - lastY) * ((double) i / steps);
                            gc.clearRect(interpX - size / 2, interpY - size / 2, size, size);
                        }
                    } else {
                        gc.clearRect(x - size / 2, y - size / 2, size, size);
                    }
                }
            } else {
                gc.setStroke(action.color);
                gc.setLineWidth(action.size);
                gc.beginPath();
                gc.moveTo(action.xPoints.get(0), action.yPoints.get(0));

                for (int i = 1; i < action.xPoints.size(); i++) {
                    gc.lineTo(action.xPoints.get(i), action.yPoints.get(i));
                }
                gc.stroke();
                gc.closePath();
            }
        }
    }

    public void setBrushColor(Color color) {
        this.brushColor = color;
    }

    public void setBrushSize(double size) {
        this.brushSize = size;
    }

    public void setEraser(boolean eraser) {
        this.eraser = eraser;
    }

    // --- คลาสย่อยสำหรับเก็บข้อมูลแต่ละเส้น ---
    private static class DrawAction {

        Color color;
        double size;
        boolean eraser;
        List<Double> xPoints = new ArrayList<>();
        List<Double> yPoints = new ArrayList<>();

        public DrawAction(Color color, double size, boolean eraser) {
            this.color = color;
            this.size = size;
            this.eraser = eraser;
        }

        public void addPoint(double x, double y) {
            xPoints.add(x);
            yPoints.add(y);
        }
    }
}
