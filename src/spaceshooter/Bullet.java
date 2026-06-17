package spaceshooter;

import java.awt.Color;
import java.awt.Graphics;

public class Bullet extends Entity {

    private final boolean enemyBullet;
    private final int speedY;
    private int panelHeight;

    public Bullet(int x, int y, boolean enemyBullet) {
        super(x, y, 6, 14);
        this.enemyBullet = enemyBullet;
        this.speedY = enemyBullet ? 6 : -10;
    }

    public void setPanelHeight(int h) { panelHeight = h; }

    @Override
    public void update() {
        y += speedY;
        if (y < -height || y > panelHeight + height) {
            active = false;
        }
    }

    @Override
    public void draw(Graphics g) {
        if (enemyBullet) {
            g.setColor(new Color(255, 80, 0));
            g.fillRect(x, y, width, height);
            g.setColor(new Color(255, 200, 0, 160));
            g.fillRect(x - 1, y - 1, width + 2, height + 2);
        } else {
            g.setColor(Color.YELLOW);
            g.fillRect(x, y, width, height);
            g.setColor(new Color(255, 255, 180, 140));
            g.fillRect(x - 1, y - 1, width + 2, height + 2);
        }
    }

    public boolean isEnemyBullet() { return enemyBullet; }
}
