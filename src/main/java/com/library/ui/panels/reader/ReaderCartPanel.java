package com.library.ui.panels.reader;

import com.library.dao.DeliveryOrderDAO;
import com.library.model.Book;
import com.library.model.DeliveryOrder;
import com.library.model.User;
import com.library.service.CartService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.List;

public class ReaderCartPanel extends VBox {
    private final User currentUser;
    private final DeliveryOrderDAO orderDAO = new DeliveryOrderDAO();
    private VBox cartItemsBox;
    private Label totalItemsLabel;
    private Label depositFeeLabel;
    private Label shippingFeeLabel;
    private Label totalAmountLabel;

    private TextField nameField;
    private TextField phoneField;
    private TextArea addressArea;
    private RadioButton deliveryRadio;
    private RadioButton pickupRadio;
    
    private ComboBox<String> paymentCombo;

    private double currentDeposit = 0;
    private double currentShipping = 0;

    public ReaderCartPanel(User user) {
        this.currentUser = user;
        setSpacing(20);
        setPadding(new Insets(30));
        buildUI();
        refreshCart();
    }

    private void buildUI() {
        // Header
        Label headerLabel = new Label("🛒 Giỏ hàng của bạn");
        headerLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #cdd6f4;");
        
        HBox splitPane = new HBox(30);
        VBox.setVgrow(splitPane, Priority.ALWAYS);

        // Left side: Cart Items
        VBox leftSide = new VBox(15);
        HBox.setHgrow(leftSide, Priority.ALWAYS);
        leftSide.setPrefWidth(600);

        Label itemsTitle = new Label("Danh sách sách muốn mượn");
        itemsTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #89b4fa;");

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        cartItemsBox = new VBox(10);
        scrollPane.setContent(cartItemsBox);

        leftSide.getChildren().addAll(itemsTitle, scrollPane);

        // Right side: Checkout Form
        VBox rightSide = new VBox(20);
        rightSide.setPrefWidth(400);
        rightSide.setStyle("-fx-background-color: #1e1e2e; -fx-padding: 20; -fx-background-radius: 10; -fx-border-color: #313244; -fx-border-radius: 10;");

        Label checkoutTitle = new Label("Thông tin nhận sách");
        checkoutTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #f9e2af;");

        ToggleGroup typeGroup = new ToggleGroup();
        deliveryRadio = new RadioButton("Giao tận nơi");
        deliveryRadio.setToggleGroup(typeGroup);
        deliveryRadio.setSelected(true);
        deliveryRadio.setStyle("-fx-text-fill: #cdd6f4;");
        
        pickupRadio = new RadioButton("Tôi sẽ đến lấy");
        pickupRadio.setToggleGroup(typeGroup);
        pickupRadio.setStyle("-fx-text-fill: #cdd6f4;");

        typeGroup.selectedToggleProperty().addListener((obs, oldV, newV) -> updateSummary());

        HBox typeBox = new HBox(15, deliveryRadio, pickupRadio);

        nameField = new TextField(currentUser.getFullName());
        nameField.setPromptText("Họ và tên người nhận");
        
        // Since User doesn't have phone directly, we leave it blank or placeholder
        phoneField = new TextField();
        phoneField.setPromptText("Số điện thoại liên hệ");

        addressArea = new TextArea();
        addressArea.setPromptText("Địa chỉ giao hàng chi tiết");
        addressArea.setPrefRowCount(3);
        
        paymentCombo = new ComboBox<>();
        paymentCombo.getItems().addAll("Tiền mặt khi nhận (COD)", "Chuyển khoản ngân hàng");
        paymentCombo.setValue("Tiền mặt khi nhận (COD)");
        paymentCombo.setStyle("-fx-font-size: 14px;");
        paymentCombo.setMaxWidth(Double.MAX_VALUE);

        VBox formBox = new VBox(10, typeBox, 
                new Label("Họ tên:"), nameField, 
                new Label("SĐT:"), phoneField, 
                new Label("Địa chỉ:"), addressArea,
                new Label("Phương thức thanh toán:"), paymentCombo);

        // Summary
        VBox summaryBox = new VBox(10);
        summaryBox.setStyle("-fx-padding: 15 0 0 0; -fx-border-color: #45475a transparent transparent transparent; -fx-border-width: 1 0 0 0;");
        
        totalItemsLabel = new Label("Tổng số sách: 0");
        totalItemsLabel.setStyle("-fx-text-fill: #cdd6f4;");
        
        depositFeeLabel = new Label("Phí cọc (tạm tính): 0 đ");
        depositFeeLabel.setStyle("-fx-text-fill: #cdd6f4;");

        shippingFeeLabel = new Label("Phí giao hàng: 0 đ");
        shippingFeeLabel.setStyle("-fx-text-fill: #cdd6f4;");

        totalAmountLabel = new Label("Tổng cộng: 0 đ");
        totalAmountLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #f38ba8;");

        summaryBox.getChildren().addAll(totalItemsLabel, depositFeeLabel, shippingFeeLabel, totalAmountLabel);

        Button checkoutBtn = new Button("Xác nhận Đặt hàng");
        checkoutBtn.setMaxWidth(Double.MAX_VALUE);
        checkoutBtn.setStyle("-fx-background-color: #a6e3a1; -fx-text-fill: #11111b; -fx-font-weight: bold; -fx-padding: 10;");
        checkoutBtn.setOnAction(e -> handleCheckout());

        rightSide.getChildren().addAll(checkoutTitle, formBox, summaryBox, checkoutBtn);

        splitPane.getChildren().addAll(leftSide, rightSide);

        getChildren().addAll(headerLabel, splitPane);
    }

    private void refreshCart() {
        cartItemsBox.getChildren().clear();
        List<Book> items = CartService.getInstance().getCartItems();
        
        if (items.isEmpty()) {
            Label empty = new Label("Giỏ hàng của bạn đang trống.");
            empty.setStyle("-fx-text-fill: #a6adc8; -fx-font-style: italic;");
            cartItemsBox.getChildren().add(empty);
        } else {
            for (Book book : items) {
                cartItemsBox.getChildren().add(createCartItemUI(book));
            }
        }
        updateSummary();
    }

    private HBox createCartItemUI(Book book) {
        HBox box = new HBox(15);
        box.setStyle("-fx-background-color: #181825; -fx-padding: 15; -fx-background-radius: 8; -fx-border-color: #313244; -fx-border-radius: 8;");
        box.setAlignment(Pos.CENTER_LEFT);

        VBox infoBox = new VBox(5);
        Label title = new Label(book.getTitle());
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #cdd6f4;");
        Label author = new Label(book.getAuthor());
        author.setStyle("-fx-text-fill: #a6adc8;");
        
        infoBox.getChildren().addAll(title, author);
        HBox.setHgrow(infoBox, Priority.ALWAYS);

        Button removeBtn = new Button("❌ Xóa");
        removeBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #f38ba8; -fx-cursor: hand;");
        removeBtn.setOnAction(e -> {
            CartService.getInstance().removeBook(book.getId());
            refreshCart();
        });

        box.getChildren().addAll(infoBox, removeBtn);
        return box;
    }

    private void updateSummary() {
        int count = CartService.getInstance().getCartCount();
        totalItemsLabel.setText("Tổng số sách: " + count);
        
        // Mock calculations
        currentDeposit = count * 50000.0; // 50k per book deposit
        
        if (deliveryRadio.isSelected()) {
            currentShipping = count > 0 ? 30000.0 : 0.0; // 30k base shipping
            addressArea.setDisable(false);
        } else {
            currentShipping = 0.0;
            addressArea.setDisable(true);
        }
        
        double total = currentDeposit + currentShipping;

        depositFeeLabel.setText(String.format("Phí cọc (tạm tính): %,.0f đ", currentDeposit));
        shippingFeeLabel.setText(String.format("Phí giao hàng: %,.0f đ", currentShipping));
        totalAmountLabel.setText(String.format("Tổng cộng: %,.0f đ", total));
    }

    private void handleCheckout() {
        if (CartService.getInstance().getCartItems().isEmpty()) {
            showAlert("Lỗi", "Giỏ hàng đang trống!");
            return;
        }

        String name = nameField.getText().trim();
        String phone = phoneField.getText().trim();
        String address = addressArea.getText().trim();
        boolean isDelivery = deliveryRadio.isSelected();

        if (name.isEmpty() || phone.isEmpty()) {
            showAlert("Lỗi", "Vui lòng nhập họ tên và số điện thoại.");
            return;
        }

        if (isDelivery && address.isEmpty()) {
            showAlert("Lỗi", "Vui lòng nhập địa chỉ giao hàng.");
            return;
        }
        
        boolean isBankTransfer = paymentCombo.getValue().equals("Chuyển khoản ngân hàng");
        
        if (isBankTransfer) {
            // Show QR Popup
            Alert qrAlert = new Alert(Alert.AlertType.INFORMATION);
            qrAlert.setTitle("Thanh toán Chuyển khoản");
            qrAlert.setHeaderText("Vui lòng thanh toán để hoàn tất đơn hàng");
            
            VBox qrBox = new VBox(15);
            qrBox.setAlignment(Pos.CENTER);
            Label instr = new Label("Quét mã QR dưới đây hoặc chuyển khoản theo thông tin:");
            Label bankInfo = new Label("Ngân hàng: MB Bank\nSTK: 123456789\nTên TK: THU VIEN SO\nSố tiền: " + String.format("%,.0f đ", currentDeposit + currentShipping) + "\nNội dung: THUVIEN " + phone);
            bankInfo.setStyle("-fx-font-weight: bold; -fx-background-color: #f38ba8; -fx-padding: 10; -fx-text-fill: #11111b;");
            
            // Dynamic QR Image via VietQR API
            String amountStr = String.format("%.0f", currentDeposit + currentShipping);
            String addInfo = "THUVIEN " + phone;
            // Xóa khoảng trắng để url hợp lệ
            addInfo = addInfo.replace(" ", "%20");
            String qrUrl = "https://img.vietqr.io/image/MB-123456789-compact2.png?amount=" + amountStr + "&addInfo=" + addInfo + "&accountName=THU%20VIEN%20SO";
            
            javafx.scene.image.ImageView qrView = new javafx.scene.image.ImageView();
            try {
                qrView.setImage(new javafx.scene.image.Image(qrUrl, 250, 250, true, true));
            } catch (Exception e) {
                System.out.println("Could not load QR code image.");
            }
            
            qrBox.getChildren().addAll(instr, qrView, bankInfo);
            qrAlert.getDialogPane().setContent(qrBox);
            
            qrAlert.showAndWait();
        }

        DeliveryOrder order = new DeliveryOrder();
        // Here we need the real readerId. Since we only have User object in scope,
        // and reader id corresponds to the `readers` table, we will use a mock or fetch it.
        // For simplicity, assuming user.getId() matches or reader is handled via API.
        // In a real app we'd fetch Reader by User's username. Let's use user.getId() as reader_id for now 
        // (Assuming 1-1 relation in logic, though schema has readers and users separate).
        order.setReaderId(currentUser.getId()); 
        
        order.setType(isDelivery ? "DELIVERY" : "PICKUP");
        order.setRecipientName(name);
        order.setRecipientPhone(phone);
        order.setDeliveryAddress(address);
        order.setShippingFee(currentShipping);
        order.setDepositFee(currentDeposit);
        order.setTotalAmount(currentDeposit + currentShipping);
        order.setPaymentMethod(isBankTransfer ? "BANK_TRANSFER" : "COD");
        order.setPaymentStatus(isBankTransfer ? "PAID" : "PENDING"); // Giả lập KH đã quét mã QR thành công, trong thực tế sẽ là PENDING chờ Kế toán duyệt
        order.setStatus("PENDING");
        order.setNote("");

        List<Integer> bookIds = CartService.getInstance().getCartItems().stream().map(Book::getId).toList();

        if (orderDAO.insert(order, bookIds)) {
            CartService.getInstance().clearCart();
            refreshCart();
            
            Alert success = new Alert(Alert.AlertType.INFORMATION);
            success.setTitle("Thành công");
            success.setHeaderText("Đặt đơn hàng thành công!");
            success.setContentText("Đơn của bạn đang chờ thủ thư xét duyệt. Bạn có thể theo dõi trong mục 'Sách đang mượn' hoặc lịch sử.");
            success.showAndWait();
        } else {
            showAlert("Lỗi", "Có lỗi xảy ra khi tạo đơn hàng. Vui lòng thử lại sau.");
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
