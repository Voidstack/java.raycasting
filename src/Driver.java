import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Line2D;
import java.awt.image.BufferStrategy;
import java.util.LinkedList;
import java.util.Random;
import javax.swing.*;

public class Driver implements Runnable, MouseMotionListener {

    private static final int WIDTH = 1260, HEIGHT = 540;
    private static final int numLines = 12;

    private static final Random random = new Random(100);

    private int mouseX = 0, mouseY = 0;

    private Canvas canvas;
    private LinkedList<Line2D.Float> lines;

    private Driver() {
        lines = buildLines();
        JFrame frame = new JFrame("Java Raycasting");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.add(canvas = new Canvas());
        canvas.addMouseMotionListener(this);
        frame.setSize(WIDTH, HEIGHT);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        new Thread(this).start();
    }

    private LinkedList<Line2D.Float> buildLines() {
        LinkedList<Line2D.Float> lines = new LinkedList<>();
        for (int i = 0; i < numLines; i++) {
            int x1 = random.nextInt(WIDTH), x2 = random.nextInt(WIDTH);
            int y1 = random.nextInt(HEIGHT), y2 = random.nextInt(HEIGHT);
            lines.add(new Line2D.Float(x1, y1, x2, y2));
        }
        return lines;
    }

    @Override
    public void run() {
        while (true) {
            render();
            try {
                // 30fps
                Thread.sleep(1000 / 30);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void render() {
        BufferStrategy bs = canvas.getBufferStrategy();
        if (bs == null) {
            canvas.createBufferStrategy(2);
            return;
        }
        Graphics g = bs.getDrawGraphics();

        g.setColor(Color.BLACK);
        g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        g.setColor(Color.GREEN);
        for (Line2D.Float line : lines) {
            g.drawLine((int) line.x1, (int) line.y1, (int) line.x2, (int) line.y2);
        }

        LinkedList<Line2D.Float> rays = calcReys(lines, mouseX, mouseY, 1000, 3000);
        for (Line2D.Float ray : rays) {
            g.drawLine((int) ray.x1, (int) ray.y1, (int) ray.x2, (int) ray.y2);
            // g.fillRect((int) ray.x2, (int) ray.y2, 20,20);
            // g.drawString("test", (int)ray.x1,(int)ray.y1 );
        }
        g.dispose();
        bs.show();
    }

    private LinkedList<Line2D.Float> calcReys(LinkedList<Line2D.Float> lines, int x, int y, int resolution, int maxDist) {
        LinkedList<Line2D.Float> rays = new LinkedList<>();

        for (int i = 0; i < resolution; i++) {
            double dir = (Math.PI * 2) * ((double) i / resolution);
            float minDist = maxDist;
            for (Line2D.Float line : lines) {
                float dist = getRayCast(x, y, x+(float) Math.cos(dir) * maxDist, y+(float) Math.sin(dir) * maxDist, line.x1, line.y1, line.x2, line.y2);
                if (dist < minDist && dist > 0) {
                    minDist = dist;
                }
            }
            rays.add(new Line2D.Float(x, y, x+(float) Math.cos(dir) * minDist, y+(float) Math.sin(dir) * minDist));
        }
        return rays;
    }


    public static void main(String[] args) {
        new Driver();
    }

    public static float dist(float x1, float y1, float x2, float y2) {
        return (float) Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));
    }

    public static float getRayCast(float p0_x, float p0_y, float p1_x, float p1_y, float p2_x, float p2_y, float p3_x, float p3_y) {
        float s1_x, s1_y, s2_x, s2_y;
        s1_x = p1_x - p0_x;
        s1_y = p1_y - p0_y;
        s2_x = p3_x - p2_x;
        s2_y = p3_y - p2_y;

        float s, t;
        s = (-s1_y * (p0_x - p2_x) + s1_x * (p0_y - p2_y)) / (-s2_x * s1_y + s1_x * s2_y);
        t = (s2_x * (p0_y - p2_y) - s2_y * (p0_x - p2_x)) / (-s2_x * s1_y + s1_x * s2_y);

        if (s >= 0 && s <= 1 && t >= 0 && t <= 1) {
            // Collision detected
            float x = p0_x + (t * s1_x);
            float y = p0_y + (t * s1_y);

            return dist(p0_x, p0_y, x, y);
        }

        return -1; // No collision
    }


    @Override
    public void mouseDragged(MouseEvent e) {
        mouseX = e.getX();
        mouseY = e.getY();
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        mouseX = e.getX();
        mouseY = e.getY();
    }
}
