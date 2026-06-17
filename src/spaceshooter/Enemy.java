package spaceshooter;

import java.awt.Color;
import java.awt.Graphics;
import java.util.Random;

public class Enemy extends Entity {

    private static final Random RNG = new Random();

    private int shootTimer;
    private final int shootInterval;

    public Enemy(int x, int y) {
        super(x, y, 36, 28);
        shootInterval = 100 + RNG.nextInt(200);
        shootTimer    = RNG.nextInt(shootInterval);
    }

    @Override
    public void update() {
        if (shootTimer > 0) shootTimer--;
    }

    @Override
    public void draw(Graphics g) {
        // Body
        g.setColor(new Color(200, 30, 30));
        g.fillRect(x + 6, y + 4, width - 12, height - 8);

        // Wings
        g.setColor(new Color(160, 0, 0));
        g.fillRect(x,              y + 8, 10, height - 16);
        g.fillRect(x + width - 10, y + 8, 10, height - 16);

        // Cockpit
        g.setColor(Color.ORANGE);
        g.fillOval(x + width / 2 - 6, y + 4, 12, 10);

        // Engine dots
        g.setColor(Color.YELLOW);
        g.fillRect(x + 8,          y + height - 6, 6, 6);
        g.fillRect(x + width - 14, y + height - 6, 6, 6);
    }

    public boolean canShoot() { return shootTimer == 0; }

    public Bullet fireBullet() {
        shootTimer = shootInterval;
        return new Bullet(x + width / 2 - 3, y + height, true);
    }

    public void moveDown(int amount) { y += amount; }
}
