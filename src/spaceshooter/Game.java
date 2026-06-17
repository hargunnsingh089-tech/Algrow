package spaceshooter;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

/**
 * Entry point — creates the window and starts the game loop.
 *
 * HOW TO RUN IN ECLIPSE:
 *   Right-click Game.java → Run As → Java Application
 */
public class Game {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Space Shooter");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setResizable(false);

            GamePanel panel = new GamePanel();
            frame.add(panel);
            frame.pack();
            frame.setLocationRelativeTo(null); // center on screen
            frame.setVisible(true);

            panel.startGame();
        });
    }
}
