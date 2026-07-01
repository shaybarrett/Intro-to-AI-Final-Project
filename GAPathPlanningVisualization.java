import java.util.*;
import java.util.List;

import javax.swing.*;
import java.awt.*;

public class GAPathPlanningVisualization {
    static int[][] grid;
    static Position start, goal;
    static JFrame frame;
    static GridPanel panel;
    static FitnessPlotPanel plotPanel;
    static JButton pauseBtn, resumeBtn, restartBtn;
    static JLabel generationLabel;
    static JSlider speedSlider;
    static GeneticAlgorithm[] ga;
    static Thread[] gaThread;

    public static void main(String[] args) {
        setupEnvironment();

        frame = new JFrame("GA Path Planning Visualization");
        panel = new GridPanel(grid, start, goal);
        plotPanel = new FitnessPlotPanel();

        pauseBtn = new JButton("Pause");
        resumeBtn = new JButton("Resume");
        restartBtn = new JButton("Restart");
        generationLabel = new JLabel("Generation: 0");
        speedSlider = new JSlider(10, 1000, 100);
        speedSlider.setMajorTickSpacing(250);
        speedSlider.setMinorTickSpacing(50);
        speedSlider.setPaintTicks(true);
        speedSlider.setPaintLabels(true);

        JPanel controls = new JPanel();
        controls.add(pauseBtn);
        controls.add(resumeBtn);
        controls.add(restartBtn);
        controls.add(generationLabel);
        controls.add(new JLabel("Speed"));
        controls.add(speedSlider);

        frame.setLayout(new BorderLayout());
        frame.add(new JScrollPane(panel), BorderLayout.CENTER);

        JPanel rightSide = new JPanel(new BorderLayout());
        rightSide.add(plotPanel, BorderLayout.CENTER);
        frame.add(rightSide, BorderLayout.EAST);

        frame.add(controls, BorderLayout.SOUTH);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        ga = new GeneticAlgorithm[]{new GeneticAlgorithm(grid, start, goal, panel, generationLabel, speedSlider, plotPanel)};
        gaThread = new Thread[]{new Thread(ga[0]::run)};
        gaThread[0].start();

        pauseBtn.addActionListener(e -> ga[0].pause());
        resumeBtn.addActionListener(e -> ga[0].resume());
        restartBtn.addActionListener(e -> restartEnvironment());
    }

    private static void setupEnvironment() {
    	 String[] options = {"Random", "Env01", "Env02", "Env03" , 
    			 "Env04", "Env05_NoObstacles"};
    	    String choice = (String) JOptionPane.showInputDialog(null, "Select Environment:", "Environment Selection", JOptionPane.PLAIN_MESSAGE, null, options, "Random");

        Random rand = new Random();

        if ("Env01".equals(choice)) {
            grid = new int[][] {
                {0,0,0,1,0,0,0,0,0},
                {0,1,0,1,0,1,1,1,0},
                {0,1,0,0,0,0,0,1,0},
                {0,1,1,1,1,1,0,1,0},
                {0,0,0,0,0,0,0,0,0}
            };
            start = new Position(0, 0);
            goal = new Position(4, 8);
        } else if ("Env02".equals(choice)) {
            grid = new int[13][30];
            for (int[] row : grid) Arrays.fill(row, 0);
            for (int i = 2; i < 11; i++) grid[i][5] = 1;
            for (int i = 2; i < 11; i++) grid[i][15] = 1;
            for (int j = 5; j <= 15; j++) grid[2][j] = 1;
            for (int j = 5; j <= 15; j++) grid[10][j] = 1;
            start = new Position(0, 0);
            goal = new Position(12, 29);
        } else if ("Env03".equals(choice)) {
        	 grid = new int[29][30];
        	    for (int[] row : grid) Arrays.fill(row, 0);
        	    for (int j = 10; j < 20; j++) {
        	    	if (j != 18) grid[15][j] = 1; // Introduce a gap in the wall
        	    }
        	    for (int i = 0; i < 14; i++) grid[i][10] = 1;
        	    for (int i = 15; i < 29; i++) grid[i][19] = 1;
        	    start = new Position(0, 0);
        	    goal = new Position(28, 29);        
       } else if ("Env04".equals(choice)) {
            grid = new int[60][50];
            for (int[] row : grid) Arrays.fill(row, 0);
            for (int i = 10; i < 50; i++) grid[i][20] = 1;
            for (int i = 10; i < 50; i++) grid[i][30] = 1;
            for (int j = 20; j <= 30; j++) grid[10][j] = 1;
            for (int j = 20; j <= 30; j++) grid[49][j] = 1;
            start = new Position(0, 0);
            goal = new Position(59, 49);
        } else if ("Env05_NoObstacles".equals(choice)) {
            grid = new int[50][50];
            for (int[] row : grid) Arrays.fill(row, 0);
            start = new Position(0, 0);
            goal = new Position(49, 49);
        }
          else {
            int rows = 20, cols = 30;
            grid = new int[rows][cols];
            for (int i = 0; i < rows; i++)
                for (int j = 0; j < cols; j++)
                    if (rand.nextDouble() < 0.2) grid[i][j] = 1;
            start = new Position(0, 0);
            goal = new Position(rows - 1, cols - 1);
        }

        grid[start.x][start.y] = 0;
        grid[goal.x][goal.y] = 0;
    }

    private static void restartEnvironment() {
        ga[0].pause();
        try { Thread.sleep(200); } catch (InterruptedException ignored) {}
        gaThread[0].interrupt();

        setupEnvironment();
        panel.grid = grid;
        panel.start = start;
        panel.goal = goal;
        panel.updateSize();
        plotPanel.clear();
        generationLabel.setText("Generation: 0");

        ga[0] = new GeneticAlgorithm(grid, start, goal, panel, generationLabel, speedSlider, plotPanel);
        gaThread[0] = new Thread(ga[0]::run);
        gaThread[0].start();
    }
}

   



class FitnessPlotPanel extends JPanel {
    List<Double> bestFitnessHistory = new ArrayList<>();
    List<Double> averageFitnessHistory = new ArrayList<>();
    int width = 300, height = 200;

    public FitnessPlotPanel() {
        setPreferredSize(new Dimension(width, height));
        setBackground(Color.WHITE);
    }

    public void updateFitness(double bestFitness, double avgFitness) {
        bestFitnessHistory.add(bestFitness);
        averageFitnessHistory.add(avgFitness);
        repaint();
    }

    public void clear() {
        bestFitnessHistory.clear();
        averageFitnessHistory.clear();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (bestFitnessHistory.isEmpty()) return;

        double maxFitness = Math.max(Collections.max(bestFitnessHistory), Collections.max(averageFitnessHistory));
        if (maxFitness == 0) maxFitness = 1;

        int margin = 20;
        int plotWidth = width - 2 * margin;
        int plotHeight = height - 2 * margin;

        g.setColor(Color.BLACK);
        g.drawRect(margin, margin, plotWidth, plotHeight);

        Graphics2D g2d = (Graphics2D) g;
        g2d.setStroke(new BasicStroke(2));

        g2d.setColor(Color.BLUE);
        for (int i = 1; i < bestFitnessHistory.size(); i++) {
            int x1 = margin + (i - 1) * plotWidth / (bestFitnessHistory.size() - 1);
            int y1 = margin + plotHeight - (int)(bestFitnessHistory.get(i - 1) / maxFitness * plotHeight);
            int x2 = margin + i * plotWidth / (bestFitnessHistory.size() - 1);
            int y2 = margin + plotHeight - (int)(bestFitnessHistory.get(i) / maxFitness * plotHeight);
            g2d.drawLine(x1, y1, x2, y2);
        }

        g2d.setColor(Color.RED);
        for (int i = 1; i < averageFitnessHistory.size(); i++) {
            int x1 = margin + (i - 1) * plotWidth / (averageFitnessHistory.size() - 1);
            int y1 = margin + plotHeight - (int)(averageFitnessHistory.get(i - 1) / maxFitness * plotHeight);
            int x2 = margin + i * plotWidth / (averageFitnessHistory.size() - 1);
            int y2 = margin + plotHeight - (int)(averageFitnessHistory.get(i) / maxFitness * plotHeight);
            g2d.drawLine(x1, y1, x2, y2);
        }

        g2d.setColor(Color.GRAY);
        for (int i = 0; i < bestFitnessHistory.size(); i++) {
            if (i % 10 == 0) {
                int x = margin + i * plotWidth / (bestFitnessHistory.size() - 1);
                g2d.fillOval(x - 2, margin + plotHeight + 2, 4, 4);
            }
        }

        // Draw legend
        int legendX = margin + 5;
        int legendY = margin + 15;
        g.setColor(Color.BLUE);
        g.fillRect(legendX, legendY, 10, 10);
        g.setColor(Color.BLACK);
        g.drawString("Best Fitness", legendX + 15, legendY + 10);

        g.setColor(Color.RED);
        g.fillRect(legendX, legendY + 20, 10, 10);
        g.setColor(Color.BLACK);
        g.drawString("Average Fitness", legendX + 15, legendY + 30);

        g.drawString(String.format("Max Fitness: %.4f", maxFitness), margin + 5, margin - 5);
    }
}


class GridPanel extends JPanel {
    int[][] grid;
    List<List<Position>> candidates = new ArrayList<>();
    List<Position> bestPath = new ArrayList<>();
    Position start, goal;
    int cellSize;

    public GridPanel(int[][] grid, Position start, Position goal) {
        this.grid = grid;
        this.start = start;
        this.goal = goal;
        updateSize();
    }

    public void updateSize() {
        int maxWidth = 800;
        int maxHeight = 600;
        cellSize = Math.min(maxWidth / grid[0].length, maxHeight / grid.length);
        setPreferredSize(new Dimension(grid[0].length * cellSize, grid.length * cellSize));
        revalidate();
        repaint();
    }

    public void updateCandidates(List<List<Position>> paths) {
        this.candidates = paths;
        repaint();
    }

    public void updateBestPath(List<Position> path) {
        this.bestPath = path;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        for (int i = 0; i < grid.length; i++) {
            for (int j = 0; j < grid[0].length; j++) {
                g.setColor(grid[i][j] == 1 ? Color.BLACK : Color.WHITE);
                g.fillRect(j * cellSize, i * cellSize, cellSize, cellSize);
                g.setColor(Color.GRAY);
                g.drawRect(j * cellSize, i * cellSize, cellSize, cellSize);
            }
        }

        g.setColor(Color.BLUE);
        for (List<Position> path : candidates) {
            for (int i = 1; i < path.size(); i++) {
                Position p1 = path.get(i - 1);
                Position p2 = path.get(i);
                g.drawLine(
                    p1.y * cellSize + cellSize/2, p1.x * cellSize + cellSize/2,
                    p2.y * cellSize + cellSize/2, p2.x * cellSize + cellSize/2
                );
            }
        }

        if (bestPath != null && bestPath.size() > 1) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.setColor(Color.ORANGE);
            g2d.setStroke(new BasicStroke(3));
            for (int i = 1; i < bestPath.size(); i++) {
                Position p1 = bestPath.get(i - 1);
                Position p2 = bestPath.get(i);
                g2d.drawLine(
                    p1.y * cellSize + cellSize/2, p1.x * cellSize + cellSize/2,
                    p2.y * cellSize + cellSize/2, p2.x * cellSize + cellSize/2
                );
            }
        }

        g.setColor(Color.GREEN);
        g.fillOval(start.y * cellSize + 4, start.x * cellSize + 4, cellSize-8, cellSize-8);
        g.setColor(Color.RED);
        g.fillOval(goal.y * cellSize + 4, goal.x * cellSize + 4, cellSize-8, cellSize-8);
    }
}


class Position {
    int x, y;
    public Position(int x, int y) { this.x = x; this.y = y; }
    public double distance(Position other) {
        return Math.sqrt(Math.pow(x - other.x, 2) + Math.pow(y - other.y, 2));
    }
    public boolean equals(Object o) {
        if (!(o instanceof Position)) return false;
        Position p = (Position) o;
        return x == p.x && y == p.y;
    }
    public int hashCode() { return Objects.hash(x, y); }
}

class Chromosome {
    List<Position> path;
    double fitness;

    public Chromosome(List<Position> path) {
        this.path = path;
    }

    public void calculateFitness(int[][] grid, Position goal) {
        double length = 0;
        double turns = 0;
        double safetyPenalty = 0;
        int collisionPenalty = 0;

        for (int i = 1; i < path.size(); i++) {
            Position prev = path.get(i - 1);
            Position curr = path.get(i);
            length += prev.distance(curr);

            // Count turns
            if (i > 1 && directionChanged(path.get(i - 2), prev, curr)) turns++;

            // Check collisions
            if (curr.x >= 0 && curr.x < grid.length && curr.y >= 0 && curr.y < grid[0].length) {
                if (grid[curr.x][curr.y] == 1) collisionPenalty += 10;
            } else {
                collisionPenalty += 20;
            }

            // Safety First Level (adjacent obstacles)
            safetyPenalty += countAdjacentObstacles(grid, curr, 1) * 2; // More severe

            // Safety Second Level (second ring obstacles)
            safetyPenalty += countAdjacentObstacles(grid, curr, 2) * 1; // Less severe
        }

        Position lastPos = path.get(path.size() - 1);
        double distanceToGoal = lastPos.distance(goal);  // encourage reaching the goal

        double wl = 1.0, wt = 1.0, ws = 1.5, wc = 2.0, wd = 3.0; // weights
        fitness = 1.0 / (wl * length + wt * turns + ws * safetyPenalty + wc * collisionPenalty + wd * distanceToGoal);
    }


    private boolean directionChanged(Position a, Position b, Position c) {
        return (b.x - a.x != c.x - b.x) || (b.y - a.y != c.y - b.y);
    }
    
    private int countAdjacentObstacles(int[][] grid, Position pos, int distance) {
        int count = 0;
        for (int dx = -distance; dx <= distance; dx++) {
            for (int dy = -distance; dy <= distance; dy++) {
                if (dx == 0 && dy == 0) continue;
                int nx = pos.x + dx;
                int ny = pos.y + dy;
                if (nx >= 0 && nx < grid.length && ny >= 0 && ny < grid[0].length) {
                    if (grid[nx][ny] == 1) count++;
                }
            }
        }
        return count;
    }
}



class GeneticAlgorithm {
    int[][] grid;
    Position start, goal;
    int populationSize = 50, maxGenerations = 100;
    double crossoverRate = 0.8, mutationRate = 0.2;
    Random rand = new Random();
    List<Chromosome> population = new ArrayList<>();
    GridPanel panel;
    JLabel generationLabel;
    JSlider speedSlider;
    FitnessPlotPanel plotPanel;
    private volatile boolean paused = false;

    public GeneticAlgorithm(int[][] grid, Position start, Position goal, GridPanel panel, JLabel generationLabel, JSlider speedSlider, FitnessPlotPanel plotPanel) {
        this.grid = grid;
        this.start = start;
        this.goal = goal;
        this.panel = panel;
        this.generationLabel = generationLabel;
        this.speedSlider = speedSlider;
        this.plotPanel = plotPanel;
    }

    public void pause() { paused = true; }
    public void resume() { paused = false; synchronized (this) { notify(); } }

    public void run() {
        initializePopulation();
        for (int gen = 0; gen < maxGenerations; gen++) {
            evaluateFitness();
            List<Chromosome> newPopulation = new ArrayList<>();
            applyElitism(newPopulation);

            while (newPopulation.size() < populationSize) {
                Chromosome parent1 = selectParent();
                Chromosome parent2 = selectParent();
                if (rand.nextDouble() < crossoverRate)
                    newPopulation.addAll(crossover(parent1, parent2));
                else
                    newPopulation.add(new Chromosome(new ArrayList<>(parent1.path)));
            }
            for (Chromosome c : newPopulation)
                if (rand.nextDouble() < mutationRate) mutate(c);

            population = newPopulation;

            List<List<Position>> paths = new ArrayList<>();
            for (Chromosome c : population) paths.add(c.path);

            Chromosome best = getBestSolution();
            double avgFitness = population.stream().mapToDouble(c -> c.fitness).average().orElse(0);
            int generationNum = gen;
            SwingUtilities.invokeLater(() -> {
                generationLabel.setText("Gen: " + generationNum + " | Best: " + String.format("%.4f", best.fitness));
                panel.updateCandidates(paths);
                panel.updateBestPath(best.path);
                plotPanel.updateFitness(best.fitness, avgFitness);
            });

            try {
                synchronized (this) { while (paused) wait(); }
                Thread.sleep(speedSlider.getValue());
            } catch (InterruptedException ignored) {}
        }
    }

    private void initializePopulation() {
        for (int i = 0; i < populationSize; i++) {
            List<Position> path = generateRandomPath(start, goal);
            if (path == null) path = Arrays.asList(start, goal);
            population.add(new Chromosome(path));
        }
    }

    private List<Position> generateRandomPath(Position start, Position goal) {
        List<Position> path = new ArrayList<>();
        path.add(start);
        Position current = start;
        int[][] directions = {{1,0},{0,1},{-1,0},{0,-1}};

        int steps = 0;
        while (!current.equals(goal) && steps < 500) {
            List<Position> neighbors = new ArrayList<>();
            for (int[] d : directions) {
                Position next = new Position(current.x + d[0], current.y + d[1]);
                if (isFree(next)) neighbors.add(next);
            }
            if (neighbors.isEmpty()) break;
            neighbors.sort(Comparator.comparingDouble(p -> p.distance(goal)));
            current = (rand.nextDouble() < 0.2 && neighbors.size() > 1) ? neighbors.get(rand.nextInt(neighbors.size())) : neighbors.get(0);
            path.add(current);
            steps++;
        }
        if (!current.equals(goal)) path.add(goal);
        return isValidPath(path) ? path : null;
    }

    private void evaluateFitness() { for (Chromosome c : population) c.calculateFitness(grid, goal); }

    private void applyElitism(List<Chromosome> newPop) {
        population.sort(Comparator.comparingDouble(c -> -c.fitness));
        newPop.add(population.get(0));
    }

    private Chromosome selectParent() { return population.get(rand.nextInt(populationSize/2)); }

    private List<Chromosome> crossover(Chromosome p1, Chromosome p2) {
        Set<Position> set1 = new HashSet<>(p1.path.subList(1, p1.path.size() - 1));
        List<Position> commons = new ArrayList<>();
        for (int i = 1; i < p2.path.size() - 1; i++) {
            if (set1.contains(p2.path.get(i))) {
                commons.add(p2.path.get(i));
            }
        }

        if (!commons.isEmpty()) {
            Position crossoverPoint = commons.get(rand.nextInt(commons.size()));
            int idx1 = p1.path.indexOf(crossoverPoint);
            int idx2 = p2.path.indexOf(crossoverPoint);

            List<Position> newPath = new ArrayList<>();
            newPath.addAll(p1.path.subList(0, idx1));
            newPath.addAll(p2.path.subList(idx2, p2.path.size()));

            // ✅ Check connection
            if (idx1 > 0 && !areAdjacent(p1.path.get(idx1 - 1), p2.path.get(idx2))) {
                // Not a valid move → reject crossover
                return Collections.singletonList(new Chromosome(new ArrayList<>(p1.path)));
            }

            if (isValidPath(newPath)) {
                return Collections.singletonList(new Chromosome(newPath));
            }
        }
        return Collections.singletonList(new Chromosome(new ArrayList<>(p1.path)));
    }

    // Helper method:
    private boolean areAdjacent(Position a, Position b) {
        int dx = Math.abs(a.x - b.x);
        int dy = Math.abs(a.y - b.y);
        return (dx + dy) == 1; // Only allow moving one cell horizontally or vertically
    }


    private void mutate(Chromosome c) {
        if (c.path.size() <= 2) return; // no mutation if only start and goal

        int idx = rand.nextInt(c.path.size() - 2) + 1; // choose random intermediate node
        Position prev = c.path.get(idx - 1);
        Position next = c.path.get(idx + 1);

        List<Position> neighbors = new ArrayList<>();
        int[][] directions = {{1,0},{0,1},{-1,0},{0,-1}};
        for (int[] d : directions) {
            Position candidate = new Position(prev.x + d[0], prev.y + d[1]);
            if (isFree(candidate) && areAdjacent(candidate, next)) {
                neighbors.add(candidate);
            }
        }

        if (!neighbors.isEmpty()) {
            Position newMid = neighbors.get(rand.nextInt(neighbors.size()));
            c.path.set(idx, newMid);
        }
    }

    

    private List<Position> findSafePath(Position start, Position end) {
        Queue<List<Position>> queue = new LinkedList<>();
        Set<Position> visited = new HashSet<>();
        queue.add(Arrays.asList(start));
        visited.add(start);
        int[][] dirs = {{1,0},{0,1},{-1,0},{0,-1}};

        while (!queue.isEmpty()) {
            List<Position> path = queue.poll();
            Position curr = path.get(path.size()-1);
            if (curr.equals(end)) return path.subList(1, path.size());
            for (int[] d : dirs) {
                Position next = new Position(curr.x+d[0], curr.y+d[1]);
                if (!visited.contains(next) && isFree(next)) {
                    visited.add(next);
                    List<Position> newPath = new ArrayList<>(path);
                    newPath.add(next);
                    queue.add(newPath);
                }
            }
        }
        return null;
    }

    private boolean isFree(Position p) { return p.x >= 0 && p.x < grid.length && p.y >= 0 && p.y < grid[0].length && grid[p.x][p.y] == 0; }

    private boolean isValidPath(List<Position> path) { for (Position p : path) if (!isFree(p)) return false; return true; }

    public Chromosome getBestSolution() { return population.stream().max(Comparator.comparingDouble(c -> c.fitness)).orElse(null); }
}



