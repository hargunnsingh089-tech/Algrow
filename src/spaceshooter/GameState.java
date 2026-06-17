package spaceshooter;

public class GameState {

    public enum State { MENU, PLAYING, GAME_OVER, WIN }

    private static final int MAX_WAVES = 3;

    private State state;
    private int score;
    private int lives;
    private int wave;
    private int highScore;

    public GameState() {
        state = State.MENU;
    }

    public void reset() {
        score = 0;
        lives = 3;
        wave  = 1;
        state = State.PLAYING;
    }

    public void addScore(int points) {
        score += points;
        if (score > highScore) highScore = score;
    }

    public void loseLife() {
        lives--;
        if (lives <= 0) state = State.GAME_OVER;
    }

    public void nextWave() {
        wave++;
        if (wave > MAX_WAVES) state = State.WIN;
    }

    public boolean isPlaying()   { return state == State.PLAYING; }
    public State   getState()    { return state; }
    public void    setState(State s) { state = s; }
    public int     getScore()    { return score; }
    public int     getLives()    { return lives; }
    public int     getWave()     { return wave; }
    public int     getHighScore(){ return highScore; }
    public int     getMaxWaves() { return MAX_WAVES; }
}
