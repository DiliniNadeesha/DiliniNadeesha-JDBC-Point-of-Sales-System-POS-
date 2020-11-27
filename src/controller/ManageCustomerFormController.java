/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controller;

import db.DB;
import db.DBConnection;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import net.sf.jasperreports.view.JasperViewer;
import util.CustomerTM;

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * FXML Controller class
 *
 * @author dilini-nadeesha
 */
public class ManageCustomerFormController implements Initializable {

    @FXML
    private Button btnSave;
    @FXML
    private Button btnDelete;
    @FXML
    private AnchorPane root;
    @FXML
    private TextField txtCustomerId;
    @FXML
    private TextField txtCustomerName;
    @FXML
    private TextField txtCustomerAddress;

    @FXML
    private TableView<CustomerTM> tblCustomers;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        tblCustomers.getColumns().get(0).setCellValueFactory(new PropertyValueFactory<>("id"));
        tblCustomers.getColumns().get(1).setCellValueFactory(new PropertyValueFactory<>("name"));
        tblCustomers.getColumns().get(2).setCellValueFactory(new PropertyValueFactory<>("address"));

        txtCustomerId.setDisable(true);
        txtCustomerName.setDisable(true);
        txtCustomerAddress.setDisable(true);
        btnDelete.setDisable(true);
        btnSave.setDisable(true);

        try {
            loadAllCustomers();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        tblCustomers.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<CustomerTM>() {
            @Override
            public void changed(ObservableValue<? extends CustomerTM> observable, CustomerTM oldValue, CustomerTM newValue) {
                CustomerTM selectedItem = tblCustomers.getSelectionModel().getSelectedItem();

                if (selectedItem == null) {
                    btnSave.setText("Save");
                    btnDelete.setDisable(true);
                    return;
                }

                btnSave.setText("Update");
                btnSave.setDisable(false);
                btnDelete.setDisable(false);
                txtCustomerName.setDisable(false);
                txtCustomerAddress.setDisable(false);
                txtCustomerId.setText(selectedItem.getId());
                txtCustomerName.setText(selectedItem.getName());
                txtCustomerAddress.setText(selectedItem.getAddress());
            }
        });
    }

    @FXML
    public void btnReport_OnAction(ActionEvent actionEvent) throws JRException {
        JasperDesign jasperDesign = JRXmlLoader.
                load(this.getClass().
                        getResourceAsStream("/report/bean-report.jrxml"));

        JasperReport jasperReport = JasperCompileManager.compileReport(jasperDesign);

        Map<String, Object> params = new HashMap<>();
        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport,
                params, new JRBeanCollectionDataSource(tblCustomers.getItems()));

        JasperViewer.viewReport(jasperPrint);
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
        if (!txtCustomerName.getText().matches("[A-Za-z][A-Za-z. ]+")) {
            new Alert(Alert.AlertType.ERROR, "Invalid Name").show();
            return;
        }
        if (btnSave.getText().equals("Save")) {
            ObservableList<CustomerTM> customers = tblCustomers.getItems();
            CustomerTM newCustomer = new CustomerTM(
                    txtCustomerId.getText(),
                    txtCustomerName.getText(),
                    txtCustomerAddress.getText()
            );
            try {
                saveCustomer(newCustomer);
                customers.add(newCustomer);
                btnAddNew_OnAction(event);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            CustomerTM selectedCustomer = tblCustomers.getSelectionModel().getSelectedItem();
            try {
                updateCustomer(new CustomerTM(selectedCustomer.getId(),
                        txtCustomerName.getText(),
                        txtCustomerAddress.getText()));
                selectedCustomer.setName(txtCustomerName.getText());
                selectedCustomer.setAddress(txtCustomerAddress.getText());
                tblCustomers.refresh();
                btnAddNew_OnAction(event);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void btnDelete_OnAction(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                "Are you sure whether you want to delete this customer?",
                ButtonType.YES, ButtonType.NO);
        Optional<ButtonType> buttonType = alert.showAndWait();
        if (buttonType.get() == ButtonType.YES) {
            CustomerTM selectedItem = tblCustomers.getSelectionModel().getSelectedItem();
            try {
                deleteCustomer(selectedItem.getId());
                tblCustomers.getItems().remove(selectedItem);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void btnAddNew_OnAction(ActionEvent actionEvent) {
        txtCustomerId.clear();
        txtCustomerName.clear();
        txtCustomerAddress.clear();
        tblCustomers.getSelectionModel().clearSelection();
        txtCustomerName.setDisable(false);
        txtCustomerAddress.setDisable(false);
        txtCustomerName.requestFocus();
        btnSave.setDisable(false);

        // Generate a new id
        int maxId = 0;

        try {
            String lastCustomerId = getLastCustomerId();

            if (lastCustomerId == null){
                maxId = 0;
            }else{
                maxId = Integer.parseInt(lastCustomerId.replace("C",""));
            }

            maxId = maxId + 1;
            String id = "";
            if (maxId < 10) {
                id = "C00" + maxId;
            } else if (maxId < 100) {
                id = "C0" + maxId;
            } else {
                id = "C" + maxId;
            }
            txtCustomerId.setText(id);

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    //============================ DB Related Operations ==========================

    /**
     * Loading all customers from DB to Table
     */
    private void loadAllCustomers() throws SQLException {
        Connection connection = DBConnection.getInstance().getConnection();
        Statement stm = connection.createStatement();
        ResultSet rst = stm.executeQuery("SELECT * FROM Customer");

        ObservableList<CustomerTM> customers = tblCustomers.getItems();
        customers.clear();

        while (rst.next()) {
            customers.add(new CustomerTM(rst.getString(1),
                    rst.getString(2),
                    rst.getString(3)));
        }
    }

    /**
     *  Save a customer in the DB
     *
     * @param customer
     * @throws SQLException
     */
    public void saveCustomer(CustomerTM customer) throws SQLException {
        Connection connection = DBConnection.getInstance().getConnection();
        PreparedStatement pstm = connection.prepareStatement("INSERT INTO Customer VALUES (?,?,?)");
        pstm.setString(1, customer.getId());
        pstm.setString(2, customer.getName());
        pstm.setString(3, customer.getAddress());
        if (pstm.executeUpdate() == 0){
            throw new RuntimeException("Something went wrong");
        }
    }

    /**
     * Update the customer in the DB
     *
     * @param customer
     * @throws SQLException
     */
    public void updateCustomer(CustomerTM customer) throws SQLException{
        Connection connection = DBConnection.getInstance().getConnection();
        PreparedStatement pstm = connection.prepareStatement("UPDATE Customer SET name=?, address=? WHERE customerId=?");
        pstm.setString(1, customer.getName());
        pstm.setString(2, customer.getAddress());
        pstm.setString(3, customer.getId());
        if (pstm.executeUpdate() == 0){
            throw new RuntimeException("Something went wrong");
        }
    }

    /**
     *  Delete a customer in the DB
     * @param customerId
     * @throws SQLException
     */
    public void deleteCustomer(String customerId) throws SQLException{
        Connection connection = DBConnection.getInstance().getConnection();
        PreparedStatement pstm = connection.prepareStatement("DELETE FROM Customer WHERE customerId=?");
        pstm.setString(1, customerId);
        if (pstm.executeUpdate() == 0){
            throw new RuntimeException("Something went wrong");
        }
    }

    /**
     *  Return the last Customer ID from the DB
     * @return
     * @throws SQLException
     */
    public String getLastCustomerId() throws SQLException{
        Connection connection = DBConnection.getInstance().getConnection();
        PreparedStatement pstm = connection.prepareStatement("SELECT customerId FROM Customer ORDER BY customerId DESC LIMIT 1");
        ResultSet rst = pstm.executeQuery();
        if (rst.next()){
            return rst.getString(1);
        }else{
            return null;
        }
    }
}

