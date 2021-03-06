package pl.kreft.rafal.thegameoflife;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class MainController {

    @FXML
    private Pane pane;
    @FXML
    private Button button;
    @FXML
    private Button clearBtn;
    @FXML
    private Button speedUpBtn;
    @FXML
    private Button slowDownBtn;
    @FXML
    private Label statusLabel;
    @FXML
    private Label cyclesLabel;
    @FXML
    private Label speedLabel;
    @FXML
    private Label livingLabel;
    @FXML
    private Label deadLabel;


    private int size = 700;
    private int spots = 100;
    private int squareSize = size / spots;

    private int cycles = 0;
    private int speed = 1000;

    private ArrayList<Cell> cells = new ArrayList<>();
    private boolean isRunning = false;
    private boolean stop = false;


    public int countNeighbours(Cell c){
        int neighbours = 0;
        for (Cell cell : cells){
            if (!cell.equals(c) &&
                    Math.sqrt(
                            Math.pow(cell.getX()-c.getX(),2)+Math.pow(cell.getY()-c.getY(),2)
                    ) <= squareSize*Math.sqrt(2))
                neighbours++;
        }
        return neighbours;
    }


    public ArrayList<Cell> cellsToCreate(){
        ArrayList<Cell> newCells = new ArrayList();
        for (int i = 0; i < size; i+=squareSize){
            for (int j = 0; j < size; j+=squareSize){
                int x = i+squareSize/2;
                int y = j+squareSize/2;
                Cell newCell = new Cell(x, y, squareSize/3.0, new Circle());
                if (cells.stream().noneMatch(cell -> cell.getX()== x && cell.getY()== y) &&
                        countNeighbours(newCell) == 3) newCells.add(newCell);
            }
        }
        return newCells;
    }


    @FXML
    public void initialize(){

        Thread simulation = new Thread(this::runSimulation);

        for (int i=0; i < size; i+=squareSize){
            for (int j=0; j < size; j+=squareSize){
                Rectangle r = new Rectangle(i, j, squareSize, squareSize);
                r.setFill(Color.WHITE);
                r.setStroke(Color.BLACK);
                pane.getChildren().add(r);
            }
        }

        pane.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                int x = (int)(mouseEvent.getX() / squareSize) * squareSize + squareSize / 2;
                int y = (int)(mouseEvent.getY() / squareSize) * squareSize + squareSize / 2;
                if (cells.stream().noneMatch(cell -> cell.getX() == x && cell.getY() == y)){
                    Circle c = new Circle();
                    double radius = squareSize / 3.0;
                    Cell cell = new Cell(x, y, radius, c);
                    cells.add(cell);
                    pane.getChildren().add(c);
                    cell.draw();
                }
                else {
                    Cell cellToRemove =
                            cells.stream().filter(cell -> cell.getX() == x && cell.getY() == y).findAny().get();
                    pane.getChildren().remove(cellToRemove.getCircle());
                    cells.removeIf(cell -> cell.equals(cellToRemove));
                }
                livingLabel.setText("Living: "+cells.size());
            }
        });

        button.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                if (!simulation.isAlive()&&!isRunning){
                    isRunning = true;
                    statusLabel.setText("Status: Running");
                    simulation.start();
                }
                else {
                    if (stop) statusLabel.setText("Status: Running");
                    else statusLabel.setText("Status: Stopped");
                    stop = !stop;
                }
            }
        });

        clearBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                stop = true;
                cells.stream().forEach(cell -> pane.getChildren().remove(cell.getCircle()));
                cells.clear();
                cycles = 0;
                statusLabel.setText("Status: Stopped");
                cyclesLabel.setText("Cycles: 0");
                livingLabel.setText("Living: 0");
                deadLabel.setText("Dead: 0");
            }
        });

        speedUpBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                if (speed > 100) speed-=100;
                speedLabel.setText("Speed: "+speed+"ms");
            }
        });

        slowDownBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                if (speed < 2000) speed+=100;
                speedLabel.setText("Speed: "+speed+"ms");
            }
        });

    }

    void runSimulation(){

        while (isRunning) {
            if (!stop) {
                Platform.runLater(()->{
                    ArrayList<Cell> cellsToCreate = cellsToCreate();
                    List<Cell> cellsToRemove =
                            cells.stream().filter(cell -> !(countNeighbours(cell) == 2 ||
                                    countNeighbours(cell) == 3)).toList();
                    deadLabel.setText("Dead: "+cellsToRemove.size());
                    cellsToRemove.forEach(cell -> pane.getChildren().remove(cell.getCircle()));
                    cells.removeAll(cellsToRemove);
                    cellsToCreate.forEach(cell -> {pane.getChildren().add(cell.getCircle()); cell.draw();});
                    cells.addAll(cellsToCreate);
                    cellsToCreate.clear();
                    livingLabel.setText("Living: "+cells.size());
                    cycles++;
                    cyclesLabel.setText("Cycles: "+cycles);
                });
            }
            try {
                Thread.sleep(speed);
            } catch (InterruptedException e) {}
        }
    }

}