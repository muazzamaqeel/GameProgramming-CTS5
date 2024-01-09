import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class Ghost {
    // Ghost's position
    private int x, y;
    private int prevX, prevY;
    // Ghost's speed
    private static int speed;
    private Board board;
    private static int amount_of_ghosts = 0;
    private Image ghost_image;
    private Random random = new Random();
    private Point lastVisitedTile;

    private double currentX, currentY;
    private double targetX, targetY;


    public Ghost(Board board, int startX, int startY) {
        if(amount_of_ghosts == 0){
            ghost_image = new ImageIcon(Objects.requireNonNull(getClass().getResource("game/images/ghost/Ghost1.png"))).getImage();
            amount_of_ghosts++;
        }
        else if(amount_of_ghosts == 1){
            ghost_image = new ImageIcon(Objects.requireNonNull(getClass().getResource("game/images/ghost/Ghost2.png"))).getImage();
            amount_of_ghosts++;
        }
        else if(amount_of_ghosts == 2){
            ghost_image = new ImageIcon(Objects.requireNonNull(getClass().getResource("game/images/ghost/Ghost3.png"))).getImage();
            amount_of_ghosts++;
        }
        else if(amount_of_ghosts == 3){
            ghost_image = new ImageIcon(Objects.requireNonNull(getClass().getResource("game/images/ghost/Ghost4.png"))).getImage();
            amount_of_ghosts++;
        }
        else {
            ghost_image = new ImageIcon(Objects.requireNonNull(getClass().getResource("game/images/ghost/Ghost1.png"))).getImage();
            amount_of_ghosts = 0;
        }

        this.board = board;
        if(Settings.isGhostSpeedChanged()){
            this.speed = Settings.getNewGhostSpeed();
        } else {
            this.speed = 1;
        }
        this.x = startX;
        this.y = startY;
        currentX = startX;
        currentY = startY;

    }

    private void initializePosition() {
        this.x = Board.GRID_WIDTH;
        this.y = Board.GRID_HEIGHT;
    }

    //Ghost moving AI
    public void move(Pacman pacman) {
        List<Point> allPaths = calculateMostEfficientPath(pacman);
        List<Point> mostEfficientPath = findMostEfficientPath(Collections.singletonList(allPaths), pacman);

        if (mostEfficientPath != null && !mostEfficientPath.isEmpty()) {
            Point nextPosition = mostEfficientPath.get(0);

            // Check if the next position is the last visited tile
            if (!nextPosition.equals(lastVisitedTile)) {
                targetX = nextPosition.getX();
                targetY = nextPosition.getY();
                lastVisitedTile = new Point(x, y);  // Update the last visited tile
            }
        }

        double dx = targetX - currentX;
        double dy = targetY - currentY;
        double distance = Math.sqrt(dx * dx + dy * dy);

        if (distance > 0) {
            double speedX = (dx / distance) * speed;
            double speedY = (dy / distance) * speed;

            // Update the current position without rounding
            currentX += speedX;
            currentY += speedY;

            // Round the final positions
            x = (int) Math.round(currentX);
            y = (int) Math.round(currentY);
        }
    }

    private List<Point> calculateMostEfficientPath(Pacman pacman) {
        Queue<Point> queue = new LinkedList<>();
        Set<Point> visited = new HashSet<>();
        HashMap<Point, Point> parentMap = new HashMap<>();

        Point start = new Point(x, y);
        Point target = new Point((int) pacman.getX(), (int) pacman.getY());

        queue.offer(start);
        visited.add(start);

        while (!queue.isEmpty()) {
            Point current = queue.poll();

            if (current.equals(target)) {
                lastVisitedTile = current; // Update the last visited tile
                // Reconstruct the path
                return extractPaths(parentMap, start, target);
            }

            for (Point neighbor : getNeighbors(current)) {
                if (!visited.contains(neighbor)) {
                    queue.offer(neighbor);
                    visited.add(neighbor);
                    parentMap.put(neighbor, current);
                }
            }
        }

        return Collections.emptyList(); // No paths found
    }

    private List<Point> getNeighbors(Point point) {
        int[] dx = {-1, 1, 0, 0};
        int[] dy = {0, 0, -1, 1};

        List<Point> neighbors = new ArrayList<>();

        for (int i = 0; i < 4; i++) {
            int nx = point.x + dx[i];
            int ny = point.y + dy[i];
            Point neighbor = new Point(nx, ny);
            if (board.isPath(nx, ny) && !neighbor.equals(lastVisitedTile)) {
                neighbors.add(neighbor);
            }
        }

        return neighbors;
    }

    private List<Point> extractPaths(HashMap<Point, Point> parentMap, Point start, Point target) {
        List<Point> path = new ArrayList<>();
        Point current = target;

        while (current != null && !current.equals(start)) {
            path.add(0, current);
            current = parentMap.get(current);
        }

        return path;
    }

    private List<Point> findMostEfficientPath(List<List<Point>> allPaths, Pacman pacman) {
        if (allPaths.isEmpty()) {
            return null;
        }

        // Find the path with the shortest distance to pacman
        List<Point> mostEfficientPath = allPaths.get(0);
        double shortestDistance = calculateDistance(mostEfficientPath, pacman);

        for (List<Point> path : allPaths) {
            double distance = calculateDistance(path, pacman);
            if (distance < shortestDistance) {
                mostEfficientPath = path;
                shortestDistance = distance;
            }
        }

        return mostEfficientPath;
    }

    private double calculateDistance(List<Point> path, Pacman pacman) {
        // Calculate the total distance of the path to pacman
        double distance = 0;
        for (Point point : path) {
            double dx = pacman.getX() - point.getX();
            double dy = pacman.getY() - point.getY();
            distance += Math.sqrt(dx * dx + dy * dy);
        }
        return distance;
    }


    //Uncomment this for previous algorithm
/*public void move(List<Ghost> ghosts, Pacman pacman) {
    int pacmanX = (int) pacman.getX();
    int pacmanY = (int) pacman.getY();
    int newX = x + ((pacmanX > x) ? speed : (pacmanX < x) ? -speed : 0);
    int newY = y + ((pacmanY > y) ? speed : (pacmanY < y) ? -speed : 0);

    // Check for collisions with other ghosts
    for (Ghost otherGhost : ghosts) {
        if (this != otherGhost && collidesWith(otherGhost, newX, newY)) {
            // Handle collision by moving ghosts back to their previous positions
            moveBackToPreviousPosition();
            otherGhost.moveBackToPreviousPosition();
            return; // Exit the method if collision occurred
        }
    }

    // Save the current position before moving
    prevX = x;
    prevY = y;

    // Move the ghost if there are no collisions
    if (board.isPath(newX, newY)) {
        x = newX;
        y = newY;
    } else {
        // If collision occurs, try to move along the other axis
        if (board.isPath(newX, y)) {
            x = newX;
        } else if (board.isPath(x, newY)) {
            y = newY;
        }
        // If both axes are blocked, do not move (ghost stays in the same position)
    }
}*/

    private void moveBackToPreviousPosition() {
        x = prevX;
        y = prevY;
    }

    private boolean collidesWith(Ghost otherGhost, int newX, int newY) {
        Rectangle thisGhostBounds = new Rectangle(newX, newY, Board.GRID_WIDTH, Board.GRID_HEIGHT);
        Rectangle otherGhostBounds = new Rectangle(otherGhost.getX(), otherGhost.getY(), Board.GRID_WIDTH, Board.GRID_HEIGHT);

        return thisGhostBounds.intersects(otherGhostBounds);
    }





    public void draw(Graphics2D graphics2D) {
        int ghostWidth = Board.GRID_WIDTH ;  // Adjust the width
        int ghostHeight = Board.GRID_HEIGHT;  // Adjust the height

        graphics2D.setColor(Color.RED);  // Set the color (you can adjust this based on your ghost image)

        // Draw the ghost image with adjusted size
        graphics2D.drawImage(ghost_image, x, y, ghostWidth, ghostHeight, null);
    }
    public int getX() {
        return x;
    }
    public void setX(int x) {
        this.x = x;
    }
    public int getY() {
        return y;
    }
    public void setY(int y) {
        this.y = y;
    }
}
