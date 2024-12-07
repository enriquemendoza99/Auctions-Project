package Agent;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import AuctionHouse.AuctionItem;
import constants.Message;
import constants.StatusBid;
import java.util.List;

public class AgentGUI extends Application {
    private UserAgentWithGUI agent;
    private Label balanceLabel;
    private TextArea auctionLog;
    private VBox itemsContainer;

    @Override
    public void start(Stage primaryStage) {
        Parameters params = getParameters();
        List<String> args = params.getRaw();
        if (args.size() != 4) {
            System.out.println("Usage: AgentGUI <name> <initial-balance> <bank-host> <bank-port>");
            Platform.exit();
            return;
        }

        String name = args.get(0);
        double balance = Double.parseDouble(args.get(1));
        String bankHost = args.get(2);
        int bankPort = Integer.parseInt(args.get(3));

        agent = new UserAgentWithGUI(name, balance, bankHost, bankPort, this);
        agent.start();

        primaryStage.setTitle("Auction Agent - " + name);

        BorderPane mainLayout = new BorderPane();
        mainLayout.setPadding(new Insets(10));

        VBox topSection = createTopSection();
        mainLayout.setTop(topSection);

        ScrollPane itemsScroll = createItemsSection();
        mainLayout.setCenter(itemsScroll);

        VBox logSection = createLogSection();
        mainLayout.setRight(logSection);

        Scene scene = new Scene(mainLayout, 1000, 600);
        primaryStage.setScene(scene);
        primaryStage.show();

        primaryStage.setOnCloseRequest(e -> {
            agent.cleanup();
            Platform.exit();
        });
    }

    private VBox createTopSection() {
        VBox topSection = new VBox(10);
        topSection.setPadding(new Insets(10));

        balanceLabel = new Label("Loading balance...");
        balanceLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        Button refreshButton = new Button("Refresh Auctions");
        refreshButton.setOnAction(e -> agent.refreshAuctions());

        HBox controls = new HBox(10);
        controls.setAlignment(Pos.CENTER_LEFT);
        controls.getChildren().addAll(balanceLabel, refreshButton);

        topSection.getChildren().add(controls);
        return topSection;
    }

    private ScrollPane createItemsSection() {
        itemsContainer = new VBox(10);
        itemsContainer.setPadding(new Insets(10));

        ScrollPane scroll = new ScrollPane(itemsContainer);
        scroll.setFitToWidth(true);
        scroll.setPrefViewportHeight(400);

        return scroll;
    }

    private VBox createLogSection() {
        VBox logSection = new VBox(10);
        logSection.setPadding(new Insets(10));
        logSection.setPrefWidth(300);

        Label logLabel = new Label("Auction Activity Log");
        logLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        auctionLog = new TextArea();
        auctionLog.setEditable(false);
        auctionLog.setWrapText(true);
        auctionLog.setPrefRowCount(20);

        logSection.getChildren().addAll(logLabel, auctionLog);
        return logSection;
    }

    private GridPane createItemPane(String auctionKey, AuctionItem item) {
        GridPane pane = new GridPane();
        pane.setHgap(10);
        pane.setVgap(5);
        pane.setPadding(new Insets(5));
        pane.setStyle("-fx-background-color: #f8f8f8; -fx-border-color: #eee;");

        pane.add(new Label("ID: " + item.getItemId()), 0, 0);
        pane.add(new Label(item.getDescription()), 1, 0);
        pane.add(new Label(String.format("Current Bid: $%.2f", item.getCurrentBid())), 2, 0);

        TextField bidAmount = new TextField();
        bidAmount.setPrefWidth(100);
        bidAmount.setPromptText("Bid amount");

        Button bidButton = new Button("Place Bid");
        bidButton.setOnAction(e -> {
            try {
                double amount = Double.parseDouble(bidAmount.getText());
                agent.tryPlaceBid(auctionKey, item.getItemId(), amount);
                bidAmount.clear();
            } catch (NumberFormatException ex) {
                showError("Invalid bid amount");
            }
        });

        HBox bidControls = new HBox(5);
        bidControls.getChildren().addAll(bidAmount, bidButton);
        pane.add(bidControls, 3, 0);

        return pane;
    }

    public void updateBalance(double total, double available) {
        Platform.runLater(() ->
                balanceLabel.setText(String.format("Total Balance: $%.2f  |  Available: $%.2f",
                        total, available))
        );
    }

    public void updateItems(String auctionKey, List<AuctionItem> items) {
        Platform.runLater(() -> {
            itemsContainer.getChildren().clear();

            VBox auctionSection = new VBox(5);
            auctionSection.setPadding(new Insets(10));
            auctionSection.setStyle("-fx-border-color: #cccccc; -fx-border-radius: 5;");

            Label auctionLabel = new Label("Auction House: " + auctionKey);
            auctionLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
            auctionSection.getChildren().add(auctionLabel);

            for (AuctionItem item : items) {
                if (item.isAvailable()) {
                    GridPane itemPane = createItemPane(auctionKey, item);
                    auctionSection.getChildren().add(itemPane);
                }
            }

            itemsContainer.getChildren().add(auctionSection);
        });
    }

    public void addLogMessage(String message) {
        Platform.runLater(() -> {
            auctionLog.appendText(message + "\n");
            auctionLog.setScrollTop(Double.MAX_VALUE);
        });
    }

    private void showError(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
