package spaceshooter;

import java.awt.Color;
import java.awt.Graphics;

public class Player extends Entity {

    private static final int SPEED         = 5;
    private static final int SHOOT_COOLDOWN = 18;

    private final int panelWidth;
    private boolean movingLeft, movingRight;
    private int shootCooldown;

    public Player(int x, int y, int panelWidth) {
        super(x, y, 40, 40);
        this.panelWidth = panelWidth;
    }

    @Override
    public void update() {
        if (movingLeft)  x -= SPEED;
        if (movingRight) x += SPEED;

        if (x < 0)                   x = 0;
        if (x + width > panelWidth)  x = panelWidth - width;

        if (shootCooldown > 0) shootCooldown--;
    }

    @Override
    public void draw(Graphics g) {
        // Main hull
        g.setColor(Color.CYAN);
        int[] hx = { x + width / 2, x,          x + width };
        int[] hy = { y,             y + height,  y + height };
        g.fillPolygon(hx, hy, 3);

        // Cockpit
        g.setColor(new Color(0, 200, 255));
        g.fillOval(x + width / 2 - 6, y + 12, 12, 12);

        // Engine glow
        g.setColor(Color.ORANGE);
        g.fillRect(x + width / 2 - 5, y + height - 8, 10, 8);
    }

    public boolean canShoot() { return shootCooldown == 0; }

    public Bullet fireBullet() {
        shootCooldown = SHOOT_COOLDOWN;
        return new Bullet(x + width / 2 - 3, y, false);
    }

    public void setMovingLeft(boolean b)  { movingLeft  = b; }
    public void setMovingRight(boolean b) { movingRight = b; }
}
