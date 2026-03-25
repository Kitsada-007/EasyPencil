package com.easypencil.util;

import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class ScreenCaptureUtil {

    public static void saveScreenAsPng(File file, Runnable preSaveAction) {
        if (preSaveAction != null) {
            preSaveAction.run();
        }

        try {
            Robot robot = new Robot();
            Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
            BufferedImage screenFullImage = robot.createScreenCapture(screenRect);

            ImageIO.write(screenFullImage, "png", file);
            System.out.println("บันทึกภาพหน้าจอสำเร็จ: " + file.getAbsolutePath());

        } catch (Exception ex) {
            System.err.println("ไม่สามารถบันทึกภาพหน้าจอได้: " + ex.getMessage());
        }
    }
}