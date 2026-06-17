package spaceshooter;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.JPanel;

public class GamePanel extends JPanel implements Runnable, KeyListener {

    public static final int WIDTH  = 600;
    public static final int HEIGHT = 700;
    private static final int FPS   = 60;

    private Thread    gameThread;
    private boolean   running;

    private Player      player;
    private List<Enemy> enemies;
    private List<Bullet> bullets;
    private GameState   gameState;

    private int enemyDirX = 2;

    // Scrolling starfield: each entry is { x, y, size, speed }
    private final int[][] stars = new int[120][4];

    public GamePanel() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);

        gameState = new GameState();
        initStars();
        initGame();
    }

    private void initStars() {
        for (int[] s : stars) {
            s[0] = (int)(Math.random() * WIDTH);
            s[1] = (int)(Math.random() * HEIGHT);
            s[2] = (int)(Math.random() * 3) + 1;
            s[3] = s[2];           // faster stars are bigger
        }
    }

    private void initGame() {
        player    = new Player(WIDTH / 2 - 20, HEIGHT - 100, WIDTH);
        enemies   = new ArrayList<>();
        bullets   = new ArrayList<>();
        enemyDirX = 2;
        spawnWave(gameState.getWave());
    }

    private void spawnWave(int wave) {
        enemies.clear();
        int rows   = 2 + Math.min(wave, 3);
        int cols   = 8;
        int startX = 55;
        int startY = 70;
        int gapX   = (WIDTH - 110) / cols;
        int gapY   = 55;

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                enemies.add(new Enemy(startX + c * gapX, startY + r * gapY));
            }
        }
    }

    // ---------------------------------------------------------------
    // Game loop
    // ---------------------------------------------------------------

    public void startGame() {
        running    = true;
        gameThread = new Thread(this);
        gameThread.start();
    }

    @Override
    public void run() {
        long lastTime    = System.nanoTime();
        double nsPerTick = 1_000_000_000.0 / FPS;

        while (running) {
            long now   = System.nanoTime();
            lastTime   = now;

            update();
            repaint();

            long sleepMs = (long)((nsPerTick - (System.nanoTime() - now)) / 1_000_000);
            if (sleepMs > 0) {
                try { Thread.sleep(sleepMs); }
                catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            }
        }
    }

    // ---------------------------------------------------------------
    // Update
    // ---------------------------------------------------------------

    private void update() {
        // Always scroll stars
        for (int[] s : stars) {
            s[1] += s[3];
            if (s[1] > HEIGHT) s[1] = 0;
        }

        if (!gameState.isPlaying()) return;

        player.update();
        updateEnemies();
        updateBullets();
        checkWaveCleared();
    }

    private void updateEnemies() {
        // Move all enemies horizontally
        boolean hitEdge = false;
        for (Enemy e : enemies) {
            e.x += enemyDirX;
            if (e.x <= 0 || e.x + e.width >= WIDTH) hitEdge = true;
        }

        // Reverse direction and drop down one row
        if (hitEdge) {
            enemyDirX = -enemyDirX;
            for (Enemy e : enemies) e.moveDown(20);
        }

        // Let enemies shoot and update timers
        for (Enemy e : enemies) {
            e.update();
            if (e.canShoot()) {
                Bullet b = e.fireBullet();
                b.setPanelHeight(HEIGHT);
                bullets.add(b);
            }
        }

        // Enemy reached player's zone → lose a life and restart wave
        for (Enemy e : enemies) {
            if (e.getY() + e.height >= HEIGHT - 90) {
                gameState.loseLife();
                if (gameState.isPlaying()) initGame();
                return;
            }
        }
    }

    private void updateBullets() {
        Iterator<Bullet> it = bullets.iterator();
        while (it.hasNext()) {
            Bullet b = it.next();
            b.update();
            if (!b.isActive()) { it.remove(); continue; }

            if (!b.isEnemyBullet()) {
                // Player bullet vs enemies
                Iterator<Enemy> ei = enemies.iterator();
                while (ei.hasNext()) {
                    Enemy e = ei.next();
                    if (b.collidesWith(e)) {
                        ei.remove();
                        b.setActive(false);
                        gameState.addScore(100 * gameState.getWave());
                        break;
                    }
                }
            } else {
                // Enemy bullet vs player
                if (b.collidesWith(player)) {
                    b.setActive(false);
                    gameState.loseLife();
                    if (gameState.isPlaying()) {
                        player = new Player(WIDTH / 2 - 20, HEIGHT - 100, WIDTH);
                    }
                }
            }
        }
    }

    private void checkWaveCleared() {
        if (enemies.isEmpty()) {
            gameState.addScore(500 * gameState.getWave());
            gameState.nextWave();
            if (gameState.isPlaying()) {
                bullets.clear();
                player = new Player(WIDTH / 2 - 20, HEIGHT - 100, WIDTH);
                spawnWave(gameState.getWave());
            }
        }
    }

    // ---------------------------------------------------------------
    // Rendering
    // ---------------------------------------------------------------

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawStars(g);

        switch (gameState.getState()) {
            case MENU:      drawMenu(g);                     break;
            case PLAYING:   drawGame(g);                     break;
            case GAME_OVER: drawGame(g); drawGameOver(g);    break;
            case WIN:       drawWin(g);                      break;
        }
    }

    private void drawStars(Graphics g) {
        for (int[] s : stars) {
            int brightness = 150 + s[2] * 35;
            g.setColor(new Color(brightness, brightness, brightness));
            g.fillRect(s[0], s[1], s[2], s[2]);
        }
    }

    private void drawGame(Graphics g) {
        for (Enemy e : enemies) e.draw(g);
        for (Bullet b : bullets) b.draw(g);
        player.draw(g);
        drawHUD(g);
    }

    private void drawHUD(Graphics g) {
        // Bottom bar
        g.setColor(new Color(30, 30, 60));
        g.fillRect(0, HEIGHT - 50, WIDTH, 50);
        g.setColor(new Color(0, 100, 200));
        g.drawLine(0, HEIGHT - 51, WIDTH, HEIGHT - 51);

        // Score
        g.setColor(Color.WHITE);
        g.setFont(new Font("Monospaced", Font.BOLD, 16));
        g.drawString("SCORE: " + gameState.getScore(), 12, HEIGHT - 22);

        // Wave
        String waveStr = "WAVE " + gameState.getWave() + "/" + gameState.getMaxWaves();
        FontMetrics fm = g.getFontMetrics();
        g.drawString(waveStr, (WIDTH - fm.stringWidth(waveStr)) / 2, HEIGHT - 22);

        // Lives (mini ships)
        for (int i = 0; i < gameState.getLives(); i++) {
            int lx = WIDTH - 28 - i * 26;
            int ly = HEIGHT - 42;
            g.setColor(Color.CYAN);
            int[] px = { lx + 9, lx,      lx + 18 };
            int[] py = { ly,     ly + 18,  ly + 18 };
            g.fillPolygon(px, py, 3);
        }
    }

    private void drawMenu(Graphics g) {
        // Title
        g.setColor(Color.CYAN);
        g.setFont(new Font("Arial", Font.BOLD, 48));
        String title = "SPACE SHOOTER";
        FontMetrics fm = g.getFontMetrics();
        g.drawString(title, (WIDTH - fm.stringWidth(title)) / 2, HEIGHT / 3);

        g.setColor(Color.LIGHT_GRAY);
        g.setFont(new Font("Arial", Font.PLAIN, 18));
        String sub = "Defeat " + gameState.getMaxWaves() + " enemy waves to win!";
        fm = g.getFontMetrics();
        g.drawString(sub, (WIDTH - fm.stringWidth(sub)) / 2, HEIGHT / 3 + 46);

        // Controls
        String[] lines = { "LEFT / RIGHT Arrow  —  Move", "SPACE  —  Shoot", "", "Press  ENTER  to Start" };
        int y = HEIGHT / 2 + 10;
        for (String line : lines) {
            g.setFont(new Font("Arial", Font.PLAIN, 17));
            fm = g.getFontMetrics();
            g.setColor(line.startsWith("Press") ? Color.YELLOW : new Color(180, 220, 255));
            g.drawString(line, (WIDTH - fm.stringWidth(line)) / 2, y);
            y += 30;
        }

        if (gameState.getHighScore() > 0) {
            g.setColor(Color.ORANGE);
            g.setFont(new Font("Arial", Font.BOLD, 16));
            String hs = "High Score: " + gameState.getHighScore();
            fm = g.getFontMetrics();
            g.drawString(hs, (WIDTH - fm.stringWidth(hs)) / 2, HEIGHT - 40);
        }
    }

    private void drawGameOver(Graphics g) {
        g.setColor(new Color(0, 0, 0, 190));
        g.fillRect(0, 0, WIDTH, HEIGHT);

        g.setColor(Color.RED);
        g.setFont(new Font("Arial", Font.BOLD, 58));
        String msg = "GAME OVER";
        FontMetrics fm = g.getFontMetrics();
        g.drawString(msg, (WIDTH - fm.stringWidth(msg)) / 2, HEIGHT / 3);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.PLAIN, 26));
        String sc = "Score: " + gameState.getScore();
        fm = g.getFontMetrics();
        g.drawString(sc, (WIDTH - fm.stringWidth(sc)) / 2, HEIGHT / 2);

        g.setColor(Color.YELLOW);
        g.setFont(new Font("Arial", Font.PLAIN, 18));
        String restart = "Press ENTER to Play Again";
        fm = g.getFontMetrics();
        g.drawString(restart, (WIDTH - fm.stringWidth(restart)) / 2, HEIGHT / 2 + 50);
    }

    private void drawWin(Graphics g) {
        g.setColor(new Color(0, 180, 0));
        g.setFont(new Font("Arial", Font.BOLD, 58));
        String msg = "YOU WIN!";
        FontMetrics fm = g.getFontMetrics();
        g.drawString(msg, (WIDTH - fm.stringWidth(msg)) / 2, HEIGHT / 3);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.PLAIN, 26));
        String sc = "Final Score: " + gameState.getScore();
        fm = g.getFontMetrics();
        g.drawString(sc, (WIDTH - fm.stringWidth(sc)) / 2, HEIGHT / 2);

        g.setColor(Color.ORANGE);
        g.setFont(new Font("Arial", Font.BOLD, 18));
        String hs = "High Score: " + gameState.getHighScore();
        fm = g.getFontMetrics();
        g.drawString(hs, (WIDTH - fm.stringWidth(hs)) / 2, HEIGHT / 2 + 42);

        g.setColor(Color.YELLOW);
        g.setFont(new Font("Arial", Font.PLAIN, 18));
        String restart = "Press ENTER to Play Again";
        fm = g.getFontMetrics();
        g.drawString(restart, (WIDTH - fm.stringWidth(restart)) / 2, HEIGHT / 2 + 88);
    }

    // ---------------------------------------------------------------
    // KeyListener
    // ---------------------------------------------------------------

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();

        GameState.State st = gameState.getState();
        if (st == GameState.State.MENU || st == GameState.State.GAME_OVER || st == GameState.State.WIN) {
            if (key == KeyEvent.VK_ENTER) {
                gameState.reset();
                initGame();
            }
            return;
        }

        if (key == KeyEvent.VK_LEFT)  player.setMovingLeft(true);
        if (key == KeyEvent.VK_RIGHT) player.setMovingRight(true);
        if (key == KeyEvent.VK_SPACE && player.canShoot()) {
            Bullet b = player.fireBullet();
            b.setPanelHeight(HEIGHT);
            bullets.add(b);
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_LEFT)  player.setMovingLeft(false);
        if (key == KeyEvent.VK_RIGHT) player.setMovingRight(false);
    }

    @Override public void keyTyped(KeyEvent e) {}
}
