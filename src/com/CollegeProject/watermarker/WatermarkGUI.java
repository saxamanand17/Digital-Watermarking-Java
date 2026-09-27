import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.*;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageOutputStream;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.image.BufferedImageOp;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.util.Random;

public class WatermarkGUI extends JFrame implements ActionListener {
    private JButton selectImageBtn, selectWatermarkBtn, processBtn, cropBtn;
    private JRadioButton centerRadio, topLeftRadio, bottomRightRadio;
    private JTextField copyrightText;
    private JCheckBox addCopyrightBox, addProtectionBox;
    private JSlider cropSlider, opacitySlider;
    private File selectedImage, selectedWatermark;
    private JLabel statusLabel, cropLabel, opacityLabel;
    private Random random = new Random();

    public WatermarkGUI() {
        setTitle("Image Watermarking Tool");
        setSize(400, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel panel = new JPanel();
        panel.setLayout(null);

        // Image Selection Section
        selectImageBtn = new JButton("Select Image");
        selectImageBtn.setBounds(50, 30, 300, 30);
        selectImageBtn.addActionListener(this);

        selectWatermarkBtn = new JButton("Select Watermark");
        selectWatermarkBtn.setBounds(50, 70, 300, 30);
        selectWatermarkBtn.addActionListener(this);

        // Watermark Position Section
        ButtonGroup positionGroup = new ButtonGroup();
        centerRadio = new JRadioButton("Center", true);
        topLeftRadio = new JRadioButton("Top Left");
        bottomRightRadio = new JRadioButton("Bottom Right");

        centerRadio.setBounds(50, 120, 100, 30);
        topLeftRadio.setBounds(150, 120, 100, 30);
        bottomRightRadio.setBounds(250, 120, 100, 30);

        positionGroup.add(centerRadio);
        positionGroup.add(topLeftRadio);
        positionGroup.add(bottomRightRadio);

        // Copyright Section
        JLabel copyrightLabel = new JLabel("Copyright Text:");
        copyrightLabel.setBounds(50, 170, 300, 20);

        copyrightText = new JTextField("© 2024 All Rights Reserved");
        copyrightText.setBounds(50, 195, 200, 30);

        addCopyrightBox = new JCheckBox("Add Copyright");
        addCopyrightBox.setBounds(260, 195, 120, 30);

        // Protection Options
        addProtectionBox = new JCheckBox("Enable Advanced Protection");
        addProtectionBox.setBounds(50, 235, 300, 30);

        // Opacity Control
        opacityLabel = new JLabel("Watermark Opacity: 30%");
        opacityLabel.setBounds(50, 275, 300, 20);

        opacitySlider = new JSlider(10, 90, 30);
        opacitySlider.setBounds(50, 300, 300, 40);
        opacitySlider.setMajorTickSpacing(20);
        opacitySlider.setPaintTicks(true);
        opacitySlider.setPaintLabels(true);
        opacitySlider.addChangeListener(e -> 
            opacityLabel.setText("Watermark Opacity: " + opacitySlider.getValue() + "%"));

        // Crop Section
        cropLabel = new JLabel("Crop Percentage: 0%");
        cropLabel.setBounds(50, 350, 300, 20);

        cropSlider = new JSlider(0, 50, 0);
        cropSlider.setBounds(50, 375, 300, 40);
        cropSlider.setMajorTickSpacing(10);
        cropSlider.setPaintTicks(true);
        cropSlider.setPaintLabels(true);
        cropSlider.addChangeListener(e -> 
            cropLabel.setText("Crop Percentage: " + cropSlider.getValue() + "%"));

        cropBtn = new JButton("Crop Image");
        cropBtn.setBounds(50, 425, 300, 30);
        cropBtn.addActionListener(this);

        // Process Button
        processBtn = new JButton("Add Watermark");
        processBtn.setBounds(50, 475, 300, 30);
        processBtn.addActionListener(this);

        // Status Label
        statusLabel = new JLabel("Select an image and watermark to begin");
        statusLabel.setBounds(50, 525, 300, 30);

        // Add all components
        panel.add(selectImageBtn);
        panel.add(selectWatermarkBtn);
        panel.add(centerRadio);
        panel.add(topLeftRadio);
        panel.add(bottomRightRadio);
        panel.add(copyrightLabel);
        panel.add(copyrightText);
        panel.add(addCopyrightBox);
        panel.add(addProtectionBox);
        panel.add(opacityLabel);
        panel.add(opacitySlider);
        panel.add(cropLabel);
        panel.add(cropSlider);
        panel.add(cropBtn);
        panel.add(processBtn);
        panel.add(statusLabel);

        add(panel);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == selectImageBtn) {
            selectedImage = chooseFile("Select Image");
            if (selectedImage != null) {
                statusLabel.setText("Image selected: " + selectedImage.getName());
            }
        } else if (e.getSource() == selectWatermarkBtn) {
            selectedWatermark = chooseFile("Select Watermark");
            if (selectedWatermark != null) {
                statusLabel.setText("Watermark selected: " + selectedWatermark.getName());
            }
        } else if (e.getSource() == cropBtn) {
            if (selectedImage == null) {
                JOptionPane.showMessageDialog(this, "Please select an image first");
                return;
            }
            cropImage();
        } else if (e.getSource() == processBtn) {
            if (selectedImage == null || selectedWatermark == null) {
                JOptionPane.showMessageDialog(this, "Please select both image and watermark");
                return;
            }
            addWatermark();
        }
    }

    private File chooseFile(String title) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle(title);
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
            "Image files", "jpg", "jpeg", "png", "gif");
        fileChooser.setFileFilter(filter);

        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            return fileChooser.getSelectedFile();
        }
        return null;
    }

    private void cropImage() {
        try {
            BufferedImage original = ImageIO.read(selectedImage);
            int cropPercentage = cropSlider.getValue();
            if (cropPercentage == 0) {
                JOptionPane.showMessageDialog(this, "Please set crop percentage first");
                return;
            }

            int cropPixels = (int)(Math.min(original.getWidth(), original.getHeight()) * (cropPercentage / 100.0));
            int newWidth = original.getWidth() - (cropPixels * 2);
            int newHeight = original.getHeight() - (cropPixels * 2);

            if (newWidth <= 0 || newHeight <= 0) {
                JOptionPane.showMessageDialog(this, "Crop percentage too high");
                return;
            }

            BufferedImage cropped = original.getSubimage(
                cropPixels, cropPixels, newWidth, newHeight);

            String croppedName = "cropped_" + selectedImage.getName();
            File output = new File(selectedImage.getParent(), croppedName);
            ImageIO.write(cropped, "PNG", output);

            selectedImage = output;
            statusLabel.setText("Image cropped: " + croppedName);

        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Error cropping image: " + ex.getMessage());
        }
    }

    private void addProtectionPattern(Graphics2D g2d, int width, int height) {
        float[] pattern = {0.1f};
        BufferedImageOp op = new ConvolveOp(new Kernel(1, 1, pattern));
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.1f));
        
        for (int i = 0; i < width; i += 50) {
            for (int j = 0; j < height; j += 50) {
                int x = i + random.nextInt(10);
                int y = j + random.nextInt(10);
                g2d.drawString("PROTECTED", x, y);
            }
        }
    }

    private void addWatermark() {
        try {
            BufferedImage image = ImageIO.read(selectedImage);
            BufferedImage watermark = ImageIO.read(selectedWatermark);

            // Resize watermark if needed
            if (watermark.getWidth() > image.getWidth() / 4 || 
                watermark.getHeight() > image.getHeight() / 4) {
                int newWidth = Math.min(watermark.getWidth(), image.getWidth() / 4);
                int newHeight = Math.min(watermark.getHeight(), image.getHeight() / 4);
                BufferedImage resizedWatermark = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2dResize = resizedWatermark.createGraphics();
                g2dResize.drawImage(watermark, 0, 0, newWidth, newHeight, null);
                g2dResize.dispose();
                watermark = resizedWatermark;
            }

            // Create output image
            BufferedImage output = new BufferedImage(
                image.getWidth(), 
                image.getHeight(), 
                BufferedImage.TYPE_INT_ARGB
            );

            // Draw original image
            Graphics2D g2d = output.createGraphics();
            g2d.drawImage(image, 0, 0, null);

            // Add protection pattern if enabled
            if (addProtectionBox.isSelected()) {
                addProtectionPattern(g2d, image.getWidth(), image.getHeight());
            }

            // Set rendering hints
            g2d.setRenderingHints(new RenderingHints(
                RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR));

            // Add watermark
            float opacity = opacitySlider.getValue() / 100.0f;
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));

            int padding = 20;
            int x = 0, y = 0;
            if (centerRadio.isSelected()) {
                x = (image.getWidth() - watermark.getWidth()) / 2;
                y = (image.getHeight() - watermark.getHeight()) / 2;
            } else if (bottomRightRadio.isSelected()) {
                x = image.getWidth() - watermark.getWidth() - padding;
                y = image.getHeight() - watermark.getHeight() - padding;
            } else if (topLeftRadio.isSelected()) {
                x = padding;
                y = padding;
            }

            g2d.drawImage(watermark, x, y, null);

            // Add copyright if selected
            if (addCopyrightBox.isSelected()) {
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.9f));
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Arial", Font.BOLD, 14));

                String copyright = copyrightText.getText();
                FontMetrics metrics = g2d.getFontMetrics();
                int textWidth = metrics.stringWidth(copyright);
                int textHeight = metrics.getHeight();

                int textX = image.getWidth() - textWidth - padding;
                int textY = image.getHeight() - padding;

                g2d.setColor(new Color(0, 0, 0, 180));
                g2d.fillRect(textX - 5, textY - textHeight, textWidth + 10, textHeight + 5);

                g2d.setColor(Color.WHITE);
                g2d.drawString(copyright, textX, textY);
            }

            g2d.dispose();

            // Save with protection
            String outputName = "protected_" + selectedImage.getName();
            ImageWriter writer = ImageIO.getImageWritersByFormatName("png").next();
            ImageWriteParam writeParam = writer.getDefaultWriteParam();
            
            File outputFile = new File(selectedImage.getParent(), outputName);
            ImageOutputStream stream = ImageIO.createImageOutputStream(outputFile);
            writer.setOutput(stream);
            
            IIOImage iioImage = new IIOImage(output, null, null);
            writer.write(null, iioImage, writeParam);
            stream.close();

            JOptionPane.showMessageDialog(this, "Image protected and watermarked successfully!\nSaved as: " + outputName);
            statusLabel.setText("Image protected and watermarked successfully!");

        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Error processing images: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        SwingUtilities.invokeLater(() -> {
            WatermarkGUI gui = new WatermarkGUI();
            gui.setVisible(true);
        });
    }
}