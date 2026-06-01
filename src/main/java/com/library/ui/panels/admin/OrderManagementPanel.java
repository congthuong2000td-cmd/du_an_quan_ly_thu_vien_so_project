package com.library.ui.panels.admin;

import com.library.dao.DeliveryOrderDAO;
import com.library.dao.DeliveryTaskDAO;
import com.library.model.DeliveryOrder;
import com.library.model.DeliveryTask;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.List;

public class OrderManagementPanel extends VBox {
    private final DeliveryOrderDAO orderDAO = new DeliveryOrderDAO();
    private final DeliveryTaskDAO taskDAO = new DeliveryTaskDAO();
    private TableView<DeliveryOrder> orderTable;

    public OrderManagementPanel() {
        setSpacing(20);
        setPadding(new Insets(20));
        buildUI();
        loadData();
    }

    private void buildUI() {
        Label title = new Label("🚚 Quản lý Đơn Giao Sách");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #cdd6f4;");

        HBox controls = new HBox(10);
        Button refreshBtn = new Button("🔄 Làm mới");
        refreshBtn.setOnAction(e -> loadData());
        controls.getChildren().add(refreshBtn);

        orderTable = new TableView<>();
        VBox.setVgrow(orderTable, Priority.ALWAYS);

        TableColumn<DeliveryOrder, String> idCol = new TableColumn<>("Mã Đơn");
        idCol.setCellValueFactory(data -> new SimpleStringProperty("#" + data.getValue().getId()));

        TableColumn<DeliveryOrder, String> readerCol = new TableColumn<>("Người nhận");
        readerCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getRecipientName()));

        TableColumn<DeliveryOrder, String> typeCol = new TableColumn<>("Loại đơn");
        typeCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getType().equals("DELIVERY") ? "Giao đi" : "Đến lấy"));
        
        TableColumn<DeliveryOrder, String> paymentCol = new TableColumn<>("Thanh toán");
        paymentCol.setCellValueFactory(data -> {
            DeliveryOrder order = data.getValue();
            String method = order.getPaymentMethod().equals("BANK_TRANSFER") ? "Chuyển khoản" : "Tiền mặt (COD)";
            String st = order.getPaymentStatus().equals("PAID") ? "Đã thu" : "Chưa thu";
            return new SimpleStringProperty(method + " - " + st);
        });

        TableColumn<DeliveryOrder, String> statusCol = new TableColumn<>("Trạng thái");
        statusCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus()));

        TableColumn<DeliveryOrder, String> actionsCol = new TableColumn<>("Hành động");
        actionsCol.setCellFactory(col -> new TableCell<>() {
            private final Button actionBtn = new Button();
            {
                actionBtn.setStyle("-fx-cursor: hand;");
                actionBtn.setOnAction(e -> {
                    DeliveryOrder order = getTableView().getItems().get(getIndex());
                    handleAction(order);
                });
            }
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    DeliveryOrder order = getTableView().getItems().get(getIndex());
                    if ("PENDING".equals(order.getStatus())) {
                        if ("BANK_TRANSFER".equals(order.getPaymentMethod()) && "PENDING".equals(order.getPaymentStatus())) {
                            actionBtn.setText("Xác nhận đã nhận tiền");
                            actionBtn.setStyle("-fx-background-color: #f9e2af; -fx-text-fill: #11111b;");
                            setGraphic(actionBtn);
                        } else {
                            actionBtn.setText("Duyệt đơn");
                            actionBtn.setStyle("-fx-background-color: #a6e3a1; -fx-text-fill: #11111b;");
                            setGraphic(actionBtn);
                        }
                    } else if ("PREPARING".equals(order.getStatus())) {
                        actionBtn.setText("Đã đóng gói (Gọi giao hàng)");
                        actionBtn.setStyle("-fx-background-color: #89b4fa; -fx-text-fill: #11111b;");
                        setGraphic(actionBtn);
                    } else {
                        setGraphic(new Label(order.getStatus()));
                    }
                }
            }
        });

        orderTable.getColumns().addAll(idCol, readerCol, typeCol, paymentCol, statusCol, actionsCol);

        getChildren().addAll(title, controls, orderTable);
    }

    private void loadData() {
        List<DeliveryOrder> orders = orderDAO.getAll();
        orderTable.setItems(FXCollections.observableArrayList(orders));
    }

    private void handleAction(DeliveryOrder order) {
        if ("PENDING".equals(order.getStatus())) {
            if ("BANK_TRANSFER".equals(order.getPaymentMethod()) && "PENDING".equals(order.getPaymentStatus())) {
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                confirm.setTitle("Xác nhận thanh toán");
                confirm.setHeaderText("Bạn xác nhận đã nhận được tiền chuyển khoản của đơn #" + order.getId() + "?");
                if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                    orderDAO.updatePaymentStatus(order.getId(), "PAID");
                    loadData();
                }
            } else {
                // Duyệt đơn
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                confirm.setTitle("Xác nhận duyệt");
                confirm.setHeaderText("Bạn muốn duyệt đơn hàng #" + order.getId() + "?");
                if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                    orderDAO.updateStatus(order.getId(), "PREPARING");
                    loadData();
                }
            }
        } else if ("PREPARING".equals(order.getStatus())) {
            // Xác nhận đóng gói xong và tạo Task cho Shipper
            ChoiceDialog<String> dialog = new ChoiceDialog<>("Giao bằng NV Thư viện", "Giao bằng NV Thư viện", "Giao qua bên trung gian (Grab/AhaMove/GHN)");
            dialog.setTitle("Chọn phương thức giao hàng");
            dialog.setHeaderText("Đơn #" + order.getId() + " đã đóng gói xong. Vui lòng chọn cách giao hàng:");
            
            dialog.showAndWait().ifPresent(choice -> {
                if (choice.equals("Giao bằng NV Thư viện")) {
                    orderDAO.updateStatus(order.getId(), "READY_FOR_DELIVERY");
                    DeliveryTask task = new DeliveryTask();
                    task.setOrderId(order.getId());
                    task.setShipperType("INTERNAL");
                    task.setStatus("ASSIGNED");
                    taskDAO.insert(task);
                    
                    loadData();
                    new Alert(Alert.AlertType.INFORMATION, "Đã tạo nhiệm vụ giao hàng cho Nhân viên thư viện!").show();
                } else {
                    // Mở form nhập Grab
                    TextInputDialog providerDialog = new TextInputDialog("GrabExpress");
                    providerDialog.setTitle("Đối tác giao hàng");
                    providerDialog.setHeaderText("Nhập tên đối tác (VD: Grab, AhaMove, ViettelPost)");
                    
                    providerDialog.showAndWait().ifPresent(provider -> {
                        TextInputDialog trackingDialog = new TextInputDialog();
                        trackingDialog.setTitle("Mã vận đơn");
                        trackingDialog.setHeaderText("Nhập Mã Vận Đơn (Tracking Code)");
                        
                        trackingDialog.showAndWait().ifPresent(tracking -> {
                            orderDAO.updateStatus(order.getId(), "DELIVERING");
                            DeliveryTask task = new DeliveryTask();
                            task.setOrderId(order.getId());
                            task.setShipperType("EXTERNAL");
                            task.setExternalProvider(provider);
                            task.setTrackingCode(tracking);
                            task.setStatus("IN_TRANSIT");
                            taskDAO.insert(task);
                            
                            loadData();
                            new Alert(Alert.AlertType.INFORMATION, "Đã lưu thông tin gửi qua " + provider + "!").show();
                        });
                    });
                }
            });
        }
    }
}
