package spaceshooter;

import java.awt.Graphics;
import java.awt.Rectangle;

/**
 * Abstract base class for all game objects (player, enemies, bullets).
 * Demonstrates OOP: abstraction, encapsulation, polymorphism.
 */
public abstract class Entity {

    protected int x, y, width, height;
    protected boolean active;

    public Entity(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.active = true;
    }

    public abstract void update();
    public abstract void draw(Graphics g);

    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }

    public boolean collidesWith(Entity other) {
        return getBounds().intersects(other.getBounds());
    }

    public boolean isActive()              { return active; }
    public void   setActive(boolean val)   { active = val; }
    public int    getX()                   { return x; }
    public int    getY()                   { return y; }
}
