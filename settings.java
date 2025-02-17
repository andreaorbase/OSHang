import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.sound.sampled.*;

public class settings {
    private JFrame newFrame;
    private static Clip clip;

    public settings() {
        playMusic("OSHang GUI/settingsMusic.wav");
        newFrame = new JFrame("Settings Window");
        newFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        newFrame.setSize(800, 525);
        newFrame.setLayout(null); // Using absolute positioning like OSHang
        newFrame.setResizable(false);
        newFrame.setLocationRelativeTo(null);

        ImageIcon background = new ImageIcon("OSHang GUI/serverRoom.gif");
        JLabel backgroundLabel = new JLabel(background);
        backgroundLabel.setBounds(0, 0, newFrame.getWidth(), newFrame.getHeight());
        newFrame.setContentPane(backgroundLabel);
        backgroundLabel.setLayout(null);

        //JButton backButton = createImageButton("OSHang GUI/playButton.png", 350, 280, 150, 50);

       /*  backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                playSound("OSHang GUI/buttonClick.wav"); 
                newFrame.dispose(); // Close current window
                new MainMenu(); // Open new window
            }
        });
        
        backgroundLabel.add(backButton); */

        newFrame.setVisible(true);
    }
    private JButton createImageButton(String imagePath, int x, int y, int width, int height) {
        ImageIcon icon = new ImageIcon(imagePath);
        Image scaledImage = icon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
        ImageIcon scaledIcon = new ImageIcon(scaledImage);

        JButton button = new JButton(scaledIcon);
        button.setBounds(x, y, width, height);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);

        return button;
    }

    private static void playSound(String filePath) {
        try {
            File audioFile = new File(filePath);
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile);
            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);
            clip.start();
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    public static void playMusic(String filePath) {
        try {
            File audioFile = new File(filePath);
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile);
            clip = AudioSystem.getClip();
            clip.open(audioStream);
            clip.loop(Clip.LOOP_CONTINUOUSLY); // Loop the music indefinitely
            clip.start();
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    public static void stopMusic() {
        if (clip != null && clip.isRunning()) {
            clip.stop();
        }
    }
}