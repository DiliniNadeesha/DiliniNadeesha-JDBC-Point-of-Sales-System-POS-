/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controller;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import db.DB;
import db.DBConnection;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import util.CustomerTM;
import util.ItemTM;

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * FXML Controller class
 *
 * @author dilini-nadeesha
 */
public class ManageItemFormController implements Initializable {

    public JFXTextField txtCode;
    public JFXTextField txtDescription;
    public JFXTextField txtQtyOnHand;
    public TableView<ItemTM> tblItems;
    public JFXTextField txtUnitPrice;

    @FXML
    private Button btnSave;
    @FXML
    private Button btnDelete;
    @FXML
    private AnchorPane root;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        tblItems.getColumns().get(0).setCellValueFactory(new PropertyValueFactory<>("code"));
        tblItems.getColumns().get(1).setCellValueFactory(new PropertyValueFactory<>("description"));
        tblItems.getColumns().get(2).setCellValueFactory(new PropertyValueFactory<>("qtyOnHand"));
        tblItems.getColumns().get(3).setCellValueFactory(new PropertyValueFactory<>("unitPrice"));

        txtCode.setDisable(true);
        txtDescription.setDisable(true);
        txtQtyOnHand.setDisable(true);
        txtUnitPrice.setDisable(true);
        btnDelete.setDisable(true);
        btnSave.setDisable(true);

        try {
            loadAllItems();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        tblItems.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<ItemTM>() {
            @Override
            public void changed(ObservableValue<? extends ItemTM> observable, ItemTM oldValue, ItemTM newValue) {
                ItemTM selectedItem = tblItems.getSelectionModel().getSelectedItem();

                if (selectedItem == null) {
                    btnSave.setText("Save");
                    btnDelete.setDisable(true);
                    return;
                }

                btnSave.setText("Update");
                btnSave.setDisable(false);
                btnDelete.setDisable(false);
                txtDescription.setDisable(false);
                txtQtyOnHand.setDisable(false);
                txtUnitPrice.setDisable(false);
                txtCode.setText(selectedItem.getCode());
                txtDescription.setText(selectedItem.getDescription());
                txtQtyOnHand.setText(selectedItem.getQtyOnHand() + "");
                txtUnitPrice.setText(selectedItem.getUnitPrice() + "");

            }
        });
    }

    @FXML
    private void navigateToHome(MouseEvent event) throws IOException {
        URL resource = this.getClass().getResource("/view/MainForm.fxml");
        Parent root = FXMLLoader.load(resource);
        Scene scene = new Scene(root);
        Stage primaryStage = (Stage) (this.root.getScene().getWindow());
        primaryStage.setScene(scene);
        primaryStage.centerOnScreen();
    }

    @FXML
    private void btnSave_OnAction(ActionEvent event) {
        if (btnSave.getText().equals("Save")) {
            ObservableList<ItemTM> items = tblItems.getItems();
            ItemTM newItem = new ItemTM(
                    txtCode.getText(),
                    txtDescription.getText(),
                    Integer.parseInt(txtQtyOnHand.getText()),
                    Double.parseDouble(txtUnitPrice.getText()));
            try {
                saveItem(newItem);
                items.add(newItem);
                btnAddNew_OnAction(event);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            ItemTM selectedItem = tblItems.getSelectionModel().getSelectedItem();
            try {
                updateItem(new ItemTM(selectedItem.getCode(),
                        txtDescription.getText(),
                        Integer.parseInt(txtQtyOnHand.getText()),
                        Double.parseDouble(txtUnitPrice.getText())));
                selectedItem.setDescription(txtDescription.getText());
                selectedItem.setQtyOnHand(Integer.parseInt(txtQtyOnHand.getText()));
                selectedItem.setUnitPrice(Double.parseDouble(txtUnitPrice.getText()));
                tblItems.refresh();
                btnAddNew_OnAction(event);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void btnDelete_OnAction(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                "Are you sure whether you want to delete this item?",
                ButtonType.YES, ButtonType.NO);
        Optional<ButtonType> buttonType = alert.showAndWait();
        if (buttonType.get() == ButtonType.YES) {
            ItemTM selectedItem = tblItems.getSelectionModel().getSelectedItem();
            try {
                deleteItem(selectedItem.getCode());
                tblItems.getItems().remove(selectedItem);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void btnAddNew_OnAction(ActionEvent actionEvent) {
        txtCode.clear();
        txtDescription.clear();
        txtQtyOnHand.clear();
        txtUnitPrice.clear();
        tblItems.getSelectionModel().clearSelection();
        txtDescription.setDisable(false);
        txtQtyOnHand.setDisable(false);
        txtUnitPrice.setDisable(false);
        txtDescription.requestFocus();
        btnSave.setDisable(false);

        // Generate a new id
        int maxCode = 0;
        try {
            String lastItemCode = getLastItemCode();
            if (lastItemCode == null){
                maxCode = 0;
            }else{
                maxCode = Integer.parseInt(lastItemCode.replace("I",""));
            }

            maxCode = maxCode + 1;
            String code = "";
            if (maxCode < 10) {
                code = "I00" + maxCode;
            } else if (maxCode < 100) {
                code = "I0" + maxCode;
            } else {
                code = "I" + maxCode;
            }
            txtCode.setText(code);
        } catch (SQLException e) {
            e.printStackTrace();
        }



    }

//============================ DB Related Operations ==========================


    /**
     * Loading all items from DB to Table
     */
    private void loadAllItems() throws SQLException {
        Connection connection = DBConnection.getInstance().getConnection();
        Statement stm = connection.createStatement();
        ResultSet rst = stm.executeQuery("SELECT * FROM Item");

        ObservableList<ItemTM> items = tblItems.getItems();
        items.clear();

        while (rst.next()) {
            items.add(new ItemTM(rst.getString(1),
                    rst.getString(2),
                    rst.getInt(4),
                    rst.getDouble(3)));
        }
    }

    /**
     *  Save an item in the DB
     *
     * @param item
     * @throws SQLException
     */
    public void saveItem(ItemTM item) throws SQLException {
        Connection connection = DBConnection.getInstance().getConnection();
        PreparedStatement pstm = connection.prepareStatement("INSERT INTO Item VALUES (?,?,?,?)");
        pstm.setString(1, item.getCode());
        pstm.setString(2, item.getDescription());
        pstm.setDouble(3, item.getUnitPrice());
        pstm.setInt(4, item.getQtyOnHand());
        if (pstm.executeUpdate() == 0){
            throw new RuntimeException("Something went wrong");
        }
    }

    /**
     * Update the item in the DB
     *
     * @param item
     * @throws SQLException
     */
    public void updateItem(ItemTM item) throws SQLException{
        Connection connection = DBConnection.getInstance().getConnection();
        PreparedStatement pstm = connection.prepareStatement("UPDATE Item SET description=?, qtyOnHand=?, unitPrice=? WHERE code=?");
        pstm.setString(1, item.getDescription());
        pstm.setInt(2, item.getQtyOnHand());
        pstm.setDouble(3, item.getUnitPrice());
        pstm.setString(4, item.getCode());
        if (pstm.executeUpdate() == 0){
            throw new RuntimeException("Something went wrong");
        }
    }

    /**
     *  Delete a item in the DB
     * @param itemCode
     * @throws SQLException
     */
    public void deleteItem(String itemCode) throws SQLException {
        Connection connection = DBConnection.getInstance().getConnection();
        PreparedStatement pstm = connection.prepareStatement("DELETE FROM Item WHERE code=?");
        pstm.setString(1, itemCode);
        if (pstm.executeUpdate() == 0){
            throw new RuntimeException("Something went wrong");
        }
    }

    /**
     *  Return the last Item Code from the DB
     * @return
     * @throws SQLException
     */
    public String getLastItemCode() throws SQLException{
        Connection connection = DBConnection.getInstance().getConnection();
        PreparedStatement pstm = connection.prepareStatement("SELECT code FROM Item ORDER BY code DESC LIMIT 1");
        ResultSet rst = pstm.executeQuery();
        if (rst.next()){
            return rst.getString(1);
        }else{
            return null;
        }
    }
}
