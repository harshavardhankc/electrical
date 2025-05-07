import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class ElectricalShopManagementSystem extends JFrame {
    private JTabbedPane tabbedPane;

    public ElectricalShopManagementSystem() {
        setTitle("Electrical Shop Management System");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Products", new ProductPanel());
        tabbedPane.addTab("Customers", new CustomerPanel());
        tabbedPane.addTab("Billing", new BillingPanel());

        add(tabbedPane);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ElectricalShopManagementSystem frame = new ElectricalShopManagementSystem();
            frame.setVisible(true);
        });
    }

    // Database connection method
    private Connection getConnection() {
        try {
            return DriverManager.getConnection("jdbc:mysql://localhost:3306/shopdb", "root", "dbms");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database Connection Failed: " + e.getMessage());
            return null;
        }
    }

    // Product Panel
    class ProductPanel extends JPanel {
        private JTextField tfName, tfPrice, tfQuantity, tfCategory;
        private JTable table;
        private DefaultTableModel model;

        public ProductPanel() {
            setLayout(new BorderLayout());
            setBackground(new Color(240, 248, 255));

            JPanel inputPanel = new JPanel(new GridLayout(6, 2, 10, 10)); // Increased rows for Update/Delete buttons
            inputPanel.setBackground(new Color(224, 255, 255));
            inputPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

            tfName = new JTextField();
            tfCategory = new JTextField();
            tfPrice = new JTextField();
            tfQuantity = new JTextField();

            inputPanel.add(new JLabel("Product Name:"));
            inputPanel.add(tfName);
            inputPanel.add(new JLabel("Category:"));
            inputPanel.add(tfCategory);
            inputPanel.add(new JLabel("Price:"));
            inputPanel.add(tfPrice);
            inputPanel.add(new JLabel("Quantity:"));
            inputPanel.add(tfQuantity);

            JButton btnAdd = new JButton("Add Product ➕");
            btnAdd.addActionListener(e -> addProduct());
            inputPanel.add(btnAdd);

            // Update and Delete buttons
            JButton btnUpdate = new JButton("Update Product ✏️");
            btnUpdate.addActionListener(e -> updateProduct());
            inputPanel.add(btnUpdate);

            JButton btnDelete = new JButton("Delete Product 🗑️");
            btnDelete.addActionListener(e -> deleteProduct());
            inputPanel.add(btnDelete);

            add(inputPanel, BorderLayout.NORTH);

            model = new DefaultTableModel(new String[]{"ID", "Name", "Category", "Price", "Quantity"}, 0);
            table = new JTable(model);
            add(new JScrollPane(table), BorderLayout.CENTER);

            loadProducts();
        }

        private void loadProducts() {
            Connection conn = getConnection();
            if (conn == null) return;

            try {
                model.setRowCount(0);
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT * FROM products");
                while (rs.next()) {
                    model.addRow(new Object[]{
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getString("category"),
                            rs.getDouble("price"),
                            rs.getInt("quantity")
                    });
                }
                conn.close();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Failed to load products: " + e.getMessage());
            }
        }

        private void addProduct() {
            String name = tfName.getText();
            String category = tfCategory.getText();
            double price = Double.parseDouble(tfPrice.getText());
            int quantity = Integer.parseInt(tfQuantity.getText());

            Connection conn = getConnection();
            if (conn == null) return;

            try {
                String sql = "INSERT INTO products (name, category, price, quantity) VALUES (?, ?, ?, ?)";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, name);
                ps.setString(2, category);
                ps.setDouble(3, price);
                ps.setInt(4, quantity);
                ps.executeUpdate();
                JOptionPane.showMessageDialog(this, "Product Added Successfully!");
                conn.close();
                loadProducts();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Failed to add product: " + e.getMessage());
            }
        }

        private void updateProduct() {
            int selectedRow = table.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Please select a product to update.");
                return;
            }

            int id = (int) model.getValueAt(selectedRow, 0);
            String name = tfName.getText();
            String category = tfCategory.getText();
            double price = Double.parseDouble(tfPrice.getText());
            int quantity = Integer.parseInt(tfQuantity.getText());

            Connection conn = getConnection();
            if (conn == null) return;

            try {
                String sql = "UPDATE products SET name = ?, category = ?, price = ?, quantity = ? WHERE id = ?";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, name);
                ps.setString(2, category);
                ps.setDouble(3, price);
                ps.setInt(4, quantity);
                ps.setInt(5, id);
                ps.executeUpdate();
                JOptionPane.showMessageDialog(this, "Product Updated Successfully!");
                conn.close();
                loadProducts();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Failed to update product: " + e.getMessage());
            }
        }

        private void deleteProduct() {
            int selectedRow = table.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Please select a product to delete.");
                return;
            }

            int id = (int) model.getValueAt(selectedRow, 0);

            int confirmation = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this product?", "Delete Product", JOptionPane.YES_NO_OPTION);
            if (confirmation == JOptionPane.YES_OPTION) {
                Connection conn = getConnection();
                if (conn == null) return;

                try {
                    String sql = "DELETE FROM products WHERE id = ?";
                    PreparedStatement ps = conn.prepareStatement(sql);
                    ps.setInt(1, id);
                    ps.executeUpdate();
                    JOptionPane.showMessageDialog(this, "Product Deleted Successfully!");
                    conn.close();
                    loadProducts();
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(this, "Failed to delete product: " + e.getMessage());
                }
            }
        }
    }

    // Customer Panel
    class CustomerPanel extends JPanel {
        private JTextField tfName, tfPhone, tfAddress;
        private JTable table;
        private DefaultTableModel model;

        public CustomerPanel() {
            setLayout(new BorderLayout());
            setBackground(new Color(255, 248, 220));

            JPanel inputPanel = new JPanel(new GridLayout(5, 2, 10, 10)); // Increased rows for Update/Delete buttons
            inputPanel.setBackground(new Color(255, 239, 213));
            inputPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

            tfName = new JTextField();
            tfPhone = new JTextField();
            tfAddress = new JTextField();

            inputPanel.add(new JLabel("Customer Name:"));
            inputPanel.add(tfName);
            inputPanel.add(new JLabel("Phone:"));
            inputPanel.add(tfPhone);
            inputPanel.add(new JLabel("Address:"));
            inputPanel.add(tfAddress);

            JButton btnAdd = new JButton("Add Customer ➕");
            btnAdd.addActionListener(e -> addCustomer());
            inputPanel.add(btnAdd);

            // Update and Delete buttons
            JButton btnUpdate = new JButton("Update Customer ✏️");
            btnUpdate.addActionListener(e -> updateCustomer());
            inputPanel.add(btnUpdate);

            JButton btnDelete = new JButton("Delete Customer 🗑️");
            btnDelete.addActionListener(e -> deleteCustomer());
            inputPanel.add(btnDelete);

            add(inputPanel, BorderLayout.NORTH);

            model = new DefaultTableModel(new String[]{"ID", "Name", "Phone", "Address"}, 0);
            table = new JTable(model);
            add(new JScrollPane(table), BorderLayout.CENTER);

            loadCustomers();
        }

        private void loadCustomers() {
            Connection conn = getConnection();
            if (conn == null) return;

            try {
                model.setRowCount(0);
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT * FROM customers");
                while (rs.next()) {
                    model.addRow(new Object[]{
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getString("phone"),
                            rs.getString("address")
                    });
                }
                conn.close();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Failed to load customers: " + e.getMessage());
            }
        }

        private void addCustomer() {
            String name = tfName.getText();
            String phone = tfPhone.getText();
            String address = tfAddress.getText();

            Connection conn = getConnection();
            if (conn == null) return;

            try {
                String sql = "INSERT INTO customers (name, phone, address) VALUES (?, ?, ?)";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, name);
                ps.setString(2, phone);
                ps.setString(3, address);
                ps.executeUpdate();
                JOptionPane.showMessageDialog(this, "Customer Added Successfully!");
                conn.close();
                loadCustomers();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Failed to add customer: " + e.getMessage());
            }
        }

        private void updateCustomer() {
            int selectedRow = table.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Please select a customer to update.");
                return;
            }

            int id = (int) model.getValueAt(selectedRow, 0);
            String name = tfName.getText();
            String phone = tfPhone.getText();
            String address = tfAddress.getText();

            Connection conn = getConnection();
            if (conn == null) return;

            try {
                String sql = "UPDATE customers SET name = ?, phone = ?, address = ? WHERE id = ?";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, name);
                ps.setString(2, phone);
                ps.setString(3, address);
                ps.setInt(4, id);
                ps.executeUpdate();
                JOptionPane.showMessageDialog(this, "Customer Updated Successfully!");
                conn.close();
                loadCustomers();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Failed to update customer: " + e.getMessage());
            }
        }

        private void deleteCustomer() {
            int selectedRow = table.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Please select a customer to delete.");
                return;
            }

            int id = (int) model.getValueAt(selectedRow, 0);

            int confirmation = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this customer?", "Delete Customer", JOptionPane.YES_NO_OPTION);
            if (confirmation == JOptionPane.YES_OPTION) {
                Connection conn = getConnection();
                if (conn == null) return;

                try {
                    String sql = "DELETE FROM customers WHERE id = ?";
                    PreparedStatement ps = conn.prepareStatement(sql);
                    ps.setInt(1, id);
                    ps.executeUpdate();
                    JOptionPane.showMessageDialog(this, "Customer Deleted Successfully!");
                    conn.close();
                    loadCustomers();
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(this, "Failed to delete customer: " + e.getMessage());
                }
            }
        }
    }

    // Billing Panel - Simple Example
    // Billing Panel with Add, Update, Delete, and Recover functionalities
        class BillingPanel extends JPanel {
        private JTextField tfCustomerId, tfDate, tfQuantity;
        private JTable table;
        private DefaultTableModel model;
        private JButton btnAddBill, btnUpdateBill, btnDeleteBill, btnRecoverBill;
        private JComboBox<String> productComboBox;
        private double totalAmount = 0;

        public BillingPanel() {
            setLayout(new BorderLayout());
            setBackground(new Color(255, 255, 240));

            // Input panel with GridLayout for alignment
            JPanel inputPanel = new JPanel(new GridLayout(5, 2, 10, 10));
            inputPanel.setBackground(new Color(255, 255, 240));
            inputPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

            tfCustomerId = new JTextField();
            tfDate = new JTextField();
            productComboBox = new JComboBox<>();
            tfQuantity = new JTextField();

            // Adding fields and labels to the input panel with proper alignment
            inputPanel.add(new JLabel("Customer ID:"));
            inputPanel.add(tfCustomerId);
            inputPanel.add(new JLabel("Date (MM/dd/yyyy):"));
            inputPanel.add(tfDate);
            inputPanel.add(new JLabel("Product:"));
            inputPanel.add(productComboBox);
            inputPanel.add(new JLabel("Quantity:"));
            inputPanel.add(tfQuantity);

            // Adding buttons panel
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
            buttonPanel.setBackground(new Color(255, 255, 240));

            btnAddBill = new JButton("Add Bill ➕");
            btnAddBill.addActionListener(e -> addBill());
            buttonPanel.add(btnAddBill);

            btnUpdateBill = new JButton("Update Bill ✏️");
            btnUpdateBill.addActionListener(e -> updateBill());
            buttonPanel.add(btnUpdateBill);

            btnDeleteBill = new JButton("Delete Bill 🗑️");
            btnDeleteBill.addActionListener(e -> deleteBill());
            buttonPanel.add(btnDeleteBill);

            btnRecoverBill = new JButton("Recover Bill 🔄");
            btnRecoverBill.addActionListener(e -> recoverBill());
            buttonPanel.add(btnRecoverBill);

            // Adding panels to the main panel
            add(inputPanel, BorderLayout.NORTH);
            add(buttonPanel, BorderLayout.SOUTH);

            // Table for displaying bills
            model = new DefaultTableModel(new String[]{"Bill ID", "Customer ID", "Date", "Total Amount"}, 0);
            table = new JTable(model);
            add(new JScrollPane(table), BorderLayout.CENTER);

            loadBills();
            loadProductsForBilling();
        }

        // Get the connection to the database
        private Connection getConnection() {
            // Replace this with your database connection logic
            return null;
        }

        private void loadProductsForBilling() {
            Connection conn = getConnection();
            if (conn == null) return;

            try {
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT name FROM products");
                while (rs.next()) {
                    productComboBox.addItem(rs.getString("name"));
                }
                conn.close();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Failed to load products: " + e.getMessage());
            }
        }

        private void loadBills() {
            Connection conn = getConnection();
            if (conn == null) return;

            try {
                model.setRowCount(0);
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT * FROM bills");
                while (rs.next()) {
                    model.addRow(new Object[]{
                            rs.getInt("id"),
                            rs.getInt("customer_id"),
                            rs.getString("date"),
                            rs.getDouble("total_amount")
                    });
                }
                conn.close();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Failed to load bills: " + e.getMessage());
            }
        }

        private void addBill() {
            try {
                // Retrieve inputs from text fields
                int customerId = Integer.parseInt(tfCustomerId.getText());
                String date = tfDate.getText();  // Example: "10/02/2009"
                String selectedProduct = (String) productComboBox.getSelectedItem();
                int quantity = Integer.parseInt(tfQuantity.getText());

                // Convert the date to the correct format (YYYY-MM-DD)
                String formattedDate = convertToDateFormat(date);
                if (formattedDate == null) {
                    JOptionPane.showMessageDialog(this, "Please enter a valid date in the format MM/dd/yyyy", "Input Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Get the product price from the database
                Connection conn = getConnection();
                if (conn == null) return;

                String productSql = "SELECT price FROM products WHERE name = ?";
                PreparedStatement ps = conn.prepareStatement(productSql);
                ps.setString(1, selectedProduct);
                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    double price = rs.getDouble("price");
                    double totalPrice = price * quantity;

                    // Insert the bill
                    String billSql = "INSERT INTO bills (customer_id, date, total_amount) VALUES (?, ?, ?)";
                    PreparedStatement billPs = conn.prepareStatement(billSql, Statement.RETURN_GENERATED_KEYS);
                    billPs.setInt(1, customerId);
                    billPs.setString(2, formattedDate);  // This is the correctly formatted date
                    billPs.setDouble(3, totalPrice);
                    billPs.executeUpdate();

                    ResultSet billRs = billPs.getGeneratedKeys();
                    if (billRs.next()) {
                        int billId = billRs.getInt(1);

                        // Insert the bill items
                        String billItemSql = "INSERT INTO bill_items (bill_id, product_name, quantity, price) VALUES (?, ?, ?, ?)";
                        PreparedStatement billItemPs = conn.prepareStatement(billItemSql);
                        billItemPs.setInt(1, billId);
                        billItemPs.setString(2, selectedProduct);
                        billItemPs.setInt(3, quantity);
                        billItemPs.setDouble(4, price);
                        billItemPs.executeUpdate();

                        JOptionPane.showMessageDialog(this, "Bill Added Successfully!");
                        loadBills();
                    }
                }
                conn.close();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Failed to add bill: " + e.getMessage());
            }
        }

        // Helper method to convert the date string to YYYY-MM-DD format
        private String convertToDateFormat(String date) {
            try {
                // Assuming the input date is in MM/dd/yyyy format (like "10/02/2009")
                SimpleDateFormat inputFormat = new SimpleDateFormat("MM/dd/yyyy");
                SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd");

                // Parse the input date
                java.util.Date parsedDate = inputFormat.parse(date);

                // Convert to the output format
                return outputFormat.format(parsedDate);
            } catch (ParseException e) {
                return null;  // Invalid date format
            }
        }

        private void updateBill() {
            int selectedRow = table.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Please select a bill to update.");
                return;
            }

            int billId = (int) model.getValueAt(selectedRow, 0);
            int customerId = Integer.parseInt(tfCustomerId.getText());
            String date = tfDate.getText();
            String selectedProduct = (String) productComboBox.getSelectedItem();
            int quantity = Integer.parseInt(tfQuantity.getText());

            // Get the product price from the database
            Connection conn = getConnection();
            if (conn == null) return;

            try {
                String productSql = "SELECT price FROM products WHERE name = ?";
                PreparedStatement ps = conn.prepareStatement(productSql);
                ps.setString(1, selectedProduct);
                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    double price = rs.getDouble("price");
                    double totalPrice = price * quantity;

                    // Update the bill
                    String billSql = "UPDATE bills SET customer_id = ?, date = ?, total_amount = ? WHERE id = ?";
                    PreparedStatement billPs = conn.prepareStatement(billSql);
                    billPs.setInt(1, customerId);
                    billPs.setString(2, date);
                    billPs.setDouble(3, totalPrice);
                    billPs.setInt(4, billId);
                    billPs.executeUpdate();

                    // Update the bill items
                    String billItemSql = "UPDATE bill_items SET quantity = ?, price = ? WHERE bill_id = ? AND product_name = ?";
                    PreparedStatement billItemPs = conn.prepareStatement(billItemSql);
                    billItemPs.setInt(1, quantity);
                    billItemPs.setDouble(2, price);
                    billItemPs.setInt(3, billId);
                    billItemPs.setString(4, selectedProduct);
                    billItemPs.executeUpdate();

                    JOptionPane.showMessageDialog(this, "Bill Updated Successfully!");
                    loadBills();
                }
                conn.close();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Failed to update bill: " + e.getMessage());
            }
        }

        private void deleteBill() {
            int selectedRow = table.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Please select a bill to delete.");
                return;
            }

            int billId = (int) model.getValueAt(selectedRow, 0);

            int confirmation = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this bill?", "Delete Bill", JOptionPane.YES_NO_OPTION);
            if (confirmation == JOptionPane.YES_OPTION) {
                Connection conn = getConnection();
                if (conn == null) return;

                try {
                    String sql = "DELETE FROM bills WHERE id = ?";
                    PreparedStatement ps = conn.prepareStatement(sql);
                    ps.setInt(1, billId);
                    ps.executeUpdate();
                    JOptionPane.showMessageDialog(this, "Bill Deleted Successfully!");
                    conn.close();
                    loadBills();
                } catch (SQLException e) {
                    JOptionPane.showMessageDialog(this, "Failed to delete bill: " + e.getMessage());
                }
            }
        }

        private void recoverBill() {
            // Placeholder for recovery logic (e.g., restoring deleted bills)
            JOptionPane.showMessageDialog(this, "Recovery feature not implemented yet.");
        }
    }
}