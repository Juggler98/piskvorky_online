import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;

public class Main {

    public static void main(String[] args) {
        int width = Piskvorky.velkostPolicka * Piskvorky.rozmer;
        int height = width + 30;

        String[] options = {"Vytvoriť novú hru", "Pripojiť sa k hre"};
        int result = JOptionPane.showOptionDialog(null, "Vytvor novú hru alebo sa k nejakej pripoj.", "Piškvorky",
                JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
        Piskvorky piskvorky = new Piskvorky();
        if (result == 0) {
            String gameId = piskvorky.createGame();
            if (gameId != null) {
                StringSelection stringSelection = new StringSelection(gameId);
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                clipboard.setContents(stringSelection, null);
                JOptionPane.showMessageDialog(null, "ID hry bolo skopírované: " + gameId);
            }
            piskvorky.listenToDatabase();
        } else if (result == 1) {
            String inputMessage = "Zadaj ID hry.";
            String gameId;
            boolean hraNajdena = false;
            while (!hraNajdena) {
                gameId = JOptionPane.showInputDialog(inputMessage);
                if (gameId == null) {
                    return;
                }
                hraNajdena = piskvorky.gameExist(gameId);
                if (hraNajdena) {
                    piskvorky.listenToDatabase();
                } else {
                    inputMessage = "Hra nenájdená. Zadaj ID hry znova.";
                }
            }
        } else {
            return;
        }

        JFrame frame;
        frame = new JFrame(piskvorky.getGameId());
        frame.setVisible(true);
        frame.setSize(width, height);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);

        frame.add(piskvorky);
        frame.addMouseListener(piskvorky);
//
//
//        Piskvorky piskvorky2 = new Piskvorky();
//        JFrame frame2 = new JFrame("Piškvorky2");
//        frame2.setVisible(true);
//        frame2.setSize(width, height);
//        frame2.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        frame2.setResizable(false);
//
//        frame2.add(piskvorky2);
//        frame2.addMouseListener(piskvorky2);
//
//        if (piskvorky2.gameExist(piskvorky.getGameId())) {
//            piskvorky2.listenToDatabase();
//        }
    }

}
