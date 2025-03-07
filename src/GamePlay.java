import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class GamePlay {
    public static boolean isSfxOn = true;
    private static Clip clip;
    public static boolean isMusicOn = true;
    private JFrame frame;
    private JLabel background;
    private JButton instructionButton, hintButton, homeButton;
    private JLabel wordLabel, statusLabel, attemptsLabel, hintLabel, scoreLabel;
    private JTextField inputField;
    private Map<Character, JLabel> keyboardMap;
    private String wordToGuess, hint;
    private char[] guessedWord;
    private Set<Character> guessedLetters;
    private int maxAttempts = 7, attemptsLeft;
    private int score = 0;
    private int wordsGuessed = 0; // New counter for words guessed

    public GamePlay() {
        playMusic("OSHang GUI/gamePlayMusic.wav");
        score = 0; // Reset score
        wordsGuessed = 0; // Reset words guessed
        loadRandomWord();
        guessedWord = new char[wordToGuess.length()];
        Arrays.fill(guessedWord, '_');
        guessedLetters = new HashSet<>();
        attemptsLeft = maxAttempts;
    
        initializeUI();
    }
    private void loadRandomWord() {
        java.util.List<String[]> words = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader("words.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(";");
                if (parts.length == 2) words.add(parts);
            }
        } catch (IOException e) {
            e.printStackTrace();
            wordToGuess = "JAVA";
            hint = "A popular programming language";
            return;
        }
        if (!words.isEmpty()) {
            Random random = new Random();
            String[] chosenWord = words.get(random.nextInt(words.size()));
            wordToGuess = chosenWord[0].toUpperCase();
            hint = chosenWord[1];
        }
    } 

    private JButton createImageButton(String imagePath, int x, int y, int width, int height) {
        ImageIcon originalIcon = new ImageIcon(imagePath);
        Image scaledImage = originalIcon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
        ImageIcon scaledIcon = new ImageIcon(scaledImage);

        JButton button = new JButton(scaledIcon);
        button.setBounds(x, y, width, height);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setOpaque(false);

        return button;
    }

    private String[] errorBackgrounds = {
        "OSHang GUI/Error GUI/errorA.png",
        "OSHang GUI/Error GUI/errorB.png",
        "OSHang GUI/Error GUI/errorC.png",
        "OSHang GUI/Error GUI/errorD.png",
        "OSHang GUI/Error GUI/errorE.png",
        "OSHang GUI/Error GUI/errorF.png"
    };

    private void initializeUI() {
        frame = new JFrame("Hangman Game");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(850, 500);
        frame.setLayout(null);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);

        // Set Background
        background = new JLabel(new ImageIcon("OSHang GUI/gameWindow.png"));
        background.setBounds(0, 0, 850, 500);
        frame.setContentPane(background);
        background.setLayout(null);

        wordLabel = createStyledLabel(getMaskedWord(), 24, 150);
        hintLabel = createStyledLabel(hint, 14, 180);
        statusLabel = createStyledLabel("Enter a letter and press Enter", 14, 210);
        attemptsLabel = createStyledLabel("Attempts left: " + attemptsLeft, 14, 240);
        scoreLabel = createStyledLabel("Score: " + score, 14, 270);

        background.add(wordLabel);
        background.add(hintLabel);
        background.add(statusLabel);
        background.add(attemptsLabel);
        background.add(scoreLabel);
        background.add(createKeyboardPanel());
        background.add(createInputFieldPanel());


        scoreLabel.setBounds(520, 17, 200, 30);
        scoreLabel.setHorizontalAlignment(SwingConstants.LEFT);


        instructionButton = createImageButton("OSHang GUI/instructionButton.png", 775, 10, 50, 50);
        instructionButton.addActionListener(e -> {
            playSound("OSHang GUI/buttonClick.wav"); 
            new InstructionWindow();
        });

        instructionButton.setPreferredSize(new Dimension(50, 50));
        instructionButton.setMinimumSize(new Dimension(50, 50));
        instructionButton.setMaximumSize(new Dimension(50, 50));

        hintButton = createImageButton("OSHang GUI/hintButton.png", 750, 300, 50, 50);
        hintButton.addActionListener(e -> {
            playSound("OSHang GUI/buttonClick.wav"); 
            if (score >= 5) {
                score -= 5; // Deduct 5 points for hint
                scoreLabel.setText("Score: " + score); // Update score display
                revealHintLetter();
            } else {
                statusLabel.setText("Not enough points to buy a hint!");
            }
        });

        hintButton.setPreferredSize(new Dimension(50, 50));
        hintButton.setMinimumSize(new Dimension(50, 50));
        hintButton.setMaximumSize(new Dimension(50, 50));

        homeButton = createImageButton("OSHang GUI/homeButton.png", 750, 360, 50, 50);
        homeButton.addActionListener(e -> {
            playSound("OSHang GUI/buttonClick.wav"); 
            stopMusic();
            frame.dispose();
            new MainMenu();
        });

        homeButton.setPreferredSize(new Dimension(50, 50));
        homeButton.setMinimumSize(new Dimension(50, 50));
        homeButton.setMaximumSize(new Dimension(50, 50));

        background.add(instructionButton);
        background.add(hintButton);
        background.add(homeButton);

        frame.setVisible(true);
    }

    private JLabel createStyledLabel(String text, int fontSize, int y) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setFont(new Font("Arial", Font.BOLD, fontSize));
        label.setForeground(Color.WHITE);
        label.setBounds(100, y, 650, 30);
        return label;
    }

    private JPanel createKeyboardPanel() {
        JPanel keyboardPanel = new JPanel(new GridLayout(3, 10, 2, 2));
        keyboardPanel.setOpaque(false);
        keyboardPanel.setBounds(225, 280, 400, 100);
        keyboardMap = new HashMap<>();
        String keyboardLayout = "QWERTYUIOPASDFGHJKLZXCVBNM";
        
        for (char letter : keyboardLayout.toCharArray()) {
            JLabel letterLabel = new JLabel(String.valueOf(letter), SwingConstants.CENTER);
            letterLabel.setFont(new Font("Arial", Font.BOLD, 14));
            letterLabel.setForeground(Color.WHITE);
            letterLabel.setOpaque(false);
            keyboardMap.put(letter, letterLabel);
            keyboardPanel.add(letterLabel);
        }
        return keyboardPanel;
    }

    private JPanel createInputFieldPanel() {
        JPanel inputPanel = new JPanel();
        inputPanel.setOpaque(false);
        inputPanel.setBounds(350, 400, 150, 50);

        inputField = new JTextField(5);
        inputField.setFont(new Font("Arial", Font.BOLD, 20));
        inputField.setHorizontalAlignment(JTextField.CENTER);
        inputField.setBackground(Color.WHITE);
        inputField.setForeground(Color.BLACK);

        // Add a KeyListener to filter input
        inputField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();

                // Allow only alphabet keys (A-Z, a-z)
                if (!Character.isLetter(c)) {
                    e.consume(); // Ignore non-alphabet keys
                }

                // Limit input to one character
                if (inputField.getText().length() >= 1) {
                    e.consume(); // Ignore input if the box already has one character
                }
            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    processGuess();
                }
            }
        });

        inputPanel.add(inputField);
        return inputPanel;
    }

    private void processGuess() {
        String input = inputField.getText().toUpperCase().trim();
        if (input.length() != 1 || !Character.isLetter(input.charAt(0))) {
            statusLabel.setText("Please enter a single valid letter.");
            return;
        }
        
        char guessedChar = input.charAt(0);
        if (guessedLetters.contains(guessedChar)) {
            statusLabel.setText("Letter already guessed. Try again.");
            return;
        }

        guessedLetters.add(guessedChar);
        boolean correctGuess = false;
        
        for (int i = 0; i < wordToGuess.length(); i++) {
            if (wordToGuess.charAt(i) == guessedChar) {
                guessedWord[i] = guessedChar;
                correctGuess = true;
            }
        }

        if (!correctGuess) {
            playSound("OSHang GUI/warningMusic.wav"); 
            attemptsLeft--;
            updateBackground();
            updateKeyboard(guessedChar, false);
        } else {
            playSound("OSHang GUI/correctMusic.wav"); 
            
            updateKeyboard(guessedChar, true);
        }

        wordLabel.setText(getMaskedWord());
        attemptsLabel.setText("Attempts left: " + attemptsLeft);
        inputField.setText("");

        checkGameStatus(); 
    }

    private void updateBackground() {
        int errorIndex = maxAttempts - attemptsLeft - 1;
        if (errorIndex >= 0 && errorIndex < errorBackgrounds.length) {

            background.setIcon(new ImageIcon(errorBackgrounds[errorIndex]));
        }

        if (errorIndex == 3) {
            updateButtonImage(instructionButton, "OSHang GUI/Error GUI/errorDinstructionButton.png");
            updateButtonImage(hintButton, "OSHang GUI/Error GUI/errorDhintButton.png");
            updateButtonImage(homeButton, "OSHang GUI/Error GUI/errorDhomeButton.png");
        } else if (errorIndex == 4) {
            updateButtonImage(instructionButton, "OSHang GUI/Error GUI/errorEinstructionButton.png");
            updateButtonImage(hintButton, "OSHang GUI/Error GUI/errorEhintButton.png");
            updateButtonImage(homeButton, "OSHang GUI/Error GUI/errorEhomeButton.png");
        }
    }

    private void updateButtonImage(JButton button, String imagePath) {
        ImageIcon icon = new ImageIcon(imagePath);
        Image img = icon.getImage().getScaledInstance(button.getWidth(), button.getHeight(), Image.SCALE_SMOOTH); 
        button.setIcon(new ImageIcon(img));

        // Reapply fixed size to prevent resizing
        button.setPreferredSize(new Dimension(50, 50));
        button.setMinimumSize(new Dimension(50, 50));
        button.setMaximumSize(new Dimension(50, 50));
    }


    private void revealHintLetter() {
        // Find the first hidden letter in the word
        for (int i = 0; i < wordToGuess.length(); i++) {
            if (guessedWord[i] == '_') {
                char revealedChar = wordToGuess.charAt(i);

                // Reveal all instances of this letter in the word
                for (int j = 0; j < wordToGuess.length(); j++) {
                    if (wordToGuess.charAt(j) == revealedChar) {
                        guessedWord[j] = revealedChar; // Reveal the letter
                    }
                }

                wordLabel.setText(getMaskedWord()); // Update the displayed word

                // Update the keyboard to mark the revealed letter as correct (green)
                updateKeyboard(revealedChar, true);
                checkGameStatus();
                return;
            }
        }
    }

    private void updateKeyboard(char guessedChar, boolean correctGuess) {
        JLabel letterLabel = keyboardMap.get(guessedChar);
        if (letterLabel != null) {
            letterLabel.setForeground(correctGuess ? Color.GREEN : Color.RED);
        }
    }

    private String getMaskedWord() {
        return String.valueOf(guessedWord).replace("", " ").trim();
    }

    private void resetUI() {
        // Reset background to the initial state
        background.setIcon(new ImageIcon("OSHang GUI/gameWindow.png"));

        // Reset button icons to their default state
        updateButtonImage(instructionButton, "OSHang GUI/instructionButton.png");
        updateButtonImage(hintButton, "OSHang GUI/hintButton.png");
        updateButtonImage(homeButton, "OSHang GUI/homeButton.png");
    }

    private void checkGameStatus() {
        if (String.valueOf(guessedWord).equals(wordToGuess)) {
            score += 10; // Award 10 points for correct guess
            wordsGuessed++; // Increment words guessed counter
            scoreLabel.setText("Score: " + score); // Update score display
    
            // Show the "You guessed the word!" message
            JLabel guessedMessage = new JLabel("You guessed the word: " + wordToGuess, SwingConstants.CENTER);
            guessedMessage.setFont(new Font("Arial", Font.BOLD, 24));
            guessedMessage.setForeground(Color.GREEN);
            guessedMessage.setBounds(200, 75, 450, 50);
            background.add(guessedMessage);
            background.revalidate();
            background.repaint();
    
            // Timer to remove the message after 3 seconds
            Timer timer = new Timer(2000, e -> {
                background.remove(guessedMessage);
                background.revalidate();
                background.repaint();
    
                // Proceed to the next word after the delay
                loadRandomWord(); 
                guessedWord = new char[wordToGuess.length()];
                Arrays.fill(guessedWord, '_');
                guessedLetters.clear();
                attemptsLeft = maxAttempts;
        
                // Reset the UI to its initial state
                resetUI();
                wordLabel.setText(getMaskedWord());
                hintLabel.setText(hint);
                attemptsLabel.setText("Attempts left: " + attemptsLeft);
        
                // Reset keyboard colors
                for (JLabel letterLabel : keyboardMap.values()) {
                    letterLabel.setForeground(Color.WHITE);
                }
            });
            timer.setRepeats(false); // Run only once
            timer.start();
        } else if (attemptsLeft <= 0) {
            stopMusic();
            // Player failed to guess the word
            new GameOver(frame, wordsGuessed); 
        }
    }

    private static void playSound(String filePath) {
        if (!settings.isSfxOn) { // Check global SFX setting
            return; // Exit without playing sound
        }
        
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
        if (!settings.isMusicOn) { // Check global SFX setting
            return; // Exit without playing sound
        }
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

    public static void main(String[] args) {
        new GamePlay();
    }
}