import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class GamePlay {
    public static boolean isSfxOn = true;
    private JFrame frame;
    private JLabel background;
    private JButton instructionButton, hintButton, homeButton;
    private JLabel wordLabel, statusLabel, attemptsLabel, hintLabel;
    private JTextField inputField;
    private Map<Character, JLabel> keyboardMap;
    private String wordToGuess, hint;
    private char[] guessedWord;
    private Set<Character> guessedLetters;
    private int maxAttempts = 7, attemptsLeft;

    public GamePlay() {
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


    // Error GUI
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

        background.add(wordLabel);
        background.add(hintLabel);
        background.add(statusLabel);
        background.add(attemptsLabel);
        background.add(createKeyboardPanel());
        background.add(createInputFieldPanel());

        instructionButton = createImageButton("OSHang GUI/instructionButton.png", 775, 10, 50, 50);
        instructionButton.addActionListener(e -> new InstructionWindow()); 

        hintButton = createImageButton("OSHang GUI/hintButton.png", 750, 300, 50, 50);
        hintButton.addActionListener(e -> revealHintLetter());

        homeButton = createImageButton("OSHang GUI/homeButton.png", 750, 360, 50, 50);
        homeButton.addActionListener(e -> {
            frame.dispose();
            new MainMenu();
        });

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
        inputField.addKeyListener(new KeyAdapter() {
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
            attemptsLeft--;
            updateBackground();
            updateKeyboard(guessedChar, false);
        } else {
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
        button.setIcon(icon);
    }

    private void revealHintLetter() {
        for (int i = 0; i < wordToGuess.length(); i++) {
            if (guessedWord[i] == '_') {
                guessedWord[i] = wordToGuess.charAt(i);
                wordLabel.setText(getMaskedWord());
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

    private void checkGameStatus() {
        if (String.valueOf(guessedWord).equals(wordToGuess)) {
            frame.dispose();
            new WordGuessed(); 
        } else if (attemptsLeft <= 0) {
            frame.dispose(); 
            new GameOver(); 
        }
    }


    public static void main(String[] args) {
        new GamePlay();
    }
}