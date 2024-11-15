package jdbcDemo;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;


public class UserAppGUI extends JFrame {
    private UserDAO userDAO;
    private JTable productTable;
    private DefaultTableModel tableModel;
    private List<ShoppingCartItem> shoppingCart; 
    private DefaultTableModel cartTableModel;

    private JComboBox<String> priceSortComboBox;
    private JComboBox<String> quantityFilterComboBox;
    
    public UserAppGUI() {
        userDAO = new UserDAO();
        shoppingCart = new ArrayList<>();
        initUI();
    }

    private void initUI() {
        setTitle("User Application");
        setSize(450, 350);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel("Welcome to Product Management App", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(titleLabel);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(1, 3, 10, 10));

        JButton loginButton = new JButton("Login");
        JButton signupButton = new JButton("Sign Up");
        JButton exitButton = new JButton("Exit");

        loginButton.addActionListener(e -> showLoginDialog());
        signupButton.addActionListener(e -> showSignupDialog());
        exitButton.addActionListener(e -> System.exit(0));

        buttonPanel.add(loginButton);
        buttonPanel.add(signupButton);
        buttonPanel.add(exitButton);

        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        mainPanel.add(buttonPanel);
        add(mainPanel);
    }

    private void showLoginDialog() {
        JTextField usernameField = new JTextField(15);
        JPasswordField passwordField = new JPasswordField(15);
        JPanel panel = new JPanel(new GridLayout(3, 2, 5, 5));

        panel.add(new JLabel("Username:"));
        panel.add(usernameField);
        panel.add(new JLabel("Password:"));
        panel.add(passwordField);

        int option = JOptionPane.showConfirmDialog(this, panel, "Login", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());
            try {
                boolean loggedIn = userDAO.loginUser(username, password);
                if (loggedIn) {
                    JOptionPane.showMessageDialog(this, "Login successful!");
                    showShoppingPage();
                } else {
                    JOptionPane.showMessageDialog(this, "Invalid credentials.");
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void showSignupDialog() {
        JTextField usernameField = new JTextField(15);
        JPasswordField passwordField = new JPasswordField(15);
        JTextField emailField = new JTextField(15);

        JPanel panel = new JPanel(new GridLayout(4, 2, 5, 5));
        panel.add(new JLabel("Username:"));
        panel.add(usernameField);
        panel.add(new JLabel("Password:"));
        panel.add(passwordField);
        panel.add(new JLabel("Email:"));
        panel.add(emailField);

        int option = JOptionPane.showConfirmDialog(this, panel, "Sign Up", JOptionPane.OK_CANCEL_OPTION);

        if (option == JOptionPane.OK_OPTION) {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());
            String email = emailField.getText();
            try {
                boolean registered = userDAO.registerUser(username, password, email);
                if (registered) {
                    JOptionPane.showMessageDialog(this, "Signup successful!");
                    showShoppingPage();
                } else {
                    JOptionPane.showMessageDialog(this, "Signup failed.");
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void showShoppingPage() {
        JFrame shoppingFrame = new JFrame("Shopping Page");
        shoppingFrame.setSize(700, 500);
        shoppingFrame.setLocationRelativeTo(this);

        JTabbedPane tabbedPane = new JTabbedPane();

        // Product Browsing Tab
        JPanel productPanel = new JPanel(new BorderLayout());

        // Filter panel
        JPanel filterPanel = new JPanel(new FlowLayout());
        JTextField nameFilterField = new JTextField(10);
        JTextField minPriceFilterField = new JTextField(5);
        JTextField maxPriceFilterField = new JTextField(5);
        JButton filterButton = new JButton("Apply Filters");

        filterPanel.add(new JLabel("Product Name:"));
        filterPanel.add(nameFilterField);
        filterPanel.add(new JLabel("Min Price:"));
        filterPanel.add(minPriceFilterField);
        filterPanel.add(new JLabel("Max Price:"));
        filterPanel.add(maxPriceFilterField);
        filterPanel.add(filterButton);

        // Product table
        tableModel = new DefaultTableModel(new Object[]{"Product ID", "Name", "Quantity", "Price"}, 0);
        productTable = new JTable(tableModel);
        refreshProductTable();

        JScrollPane tableScrollPane = new JScrollPane(productTable);
        tableScrollPane.setBorder(BorderFactory.createTitledBorder("Available Products"));

        JPanel addToCartPanel = new JPanel(new FlowLayout());
        JTextField productIdField = new JTextField(5);
        JTextField quantityField = new JTextField(5);
        JButton addToCartButton = new JButton("Add to Cart");

        addToCartPanel.add(new JLabel("Product ID:"));
        addToCartPanel.add(productIdField);
        addToCartPanel.add(new JLabel("Quantity:"));
        addToCartPanel.add(quantityField);
        addToCartPanel.add(addToCartButton);

        // Employee button
        JButton employeeButton = new JButton("Employee");
        filterPanel.add(employeeButton);

        productPanel.add(filterPanel, BorderLayout.NORTH);
        productPanel.add(tableScrollPane, BorderLayout.CENTER);
        productPanel.add(addToCartPanel, BorderLayout.SOUTH);

        tabbedPane.add("Products", productPanel);

        // Shopping Cart Tab
        JPanel cartPanel = new JPanel(new BorderLayout());

        DefaultTableModel cartTableModel = new DefaultTableModel(new Object[]{"Product ID", "Name", "Quantity", "Price", "Total Price"}, 0);
        JTable cartTable = new JTable(cartTableModel);

        JScrollPane cartScrollPane = new JScrollPane(cartTable);
        cartScrollPane.setBorder(BorderFactory.createTitledBorder("Shopping Cart"));

        JPanel checkoutPanel = new JPanel(new FlowLayout());
        JButton checkoutButton = new JButton("Checkout");
        JButton discountButton = new JButton("Discounts");

        checkoutPanel.add(discountButton);
        checkoutPanel.add(checkoutButton);

        JLabel totalCostLabel = new JLabel("Total: $0.00");
        checkoutPanel.add(totalCostLabel);

        cartPanel.add(cartScrollPane, BorderLayout.CENTER);
        cartPanel.add(checkoutPanel, BorderLayout.SOUTH);

        tabbedPane.add("Shopping Cart", cartPanel);

        shoppingFrame.add(tabbedPane);
        shoppingFrame.setVisible(true);

        // Action Listener for filter button
        filterButton.addActionListener(e -> applyFilters(nameFilterField.getText(),
                minPriceFilterField.getText(), maxPriceFilterField.getText()));

        // Action Listener to add items to cart
        addToCartButton.addActionListener(e -> {
            try {
                int productId = Integer.parseInt(productIdField.getText());
                int quantity = Integer.parseInt(quantityField.getText());

                Product selectedProduct = userDAO.getAllProducts().stream()
                        .filter(p -> p.getId() == productId)
                        .findFirst()
                        .orElse(null);

                if (selectedProduct != null && selectedProduct.getQuantity() >= quantity) {
                    double totalPrice = selectedProduct.getPrice() * quantity;
                    cartTableModel.addRow(new Object[]{
                            selectedProduct.getId(),
                            selectedProduct.getName(),
                            quantity,
                            selectedProduct.getPrice(),
                            totalPrice
                    });
                    updateTotalCost(cartTableModel, totalCostLabel);
                    JOptionPane.showMessageDialog(shoppingFrame, "Added to cart!");
                } else {
                    JOptionPane.showMessageDialog(shoppingFrame, "Product not available in the required quantity.");
                }
            } catch (NumberFormatException | SQLException ex) {
                ex.printStackTrace();
            }
        });

        // Action Listener for checkout
        checkoutButton.addActionListener(e -> {
            try {
                boolean success = true;
                for (int i = 0; i < cartTableModel.getRowCount(); i++) {
                    int productId = (int) cartTableModel.getValueAt(i, 0);
                    int quantity = (int) cartTableModel.getValueAt(i, 2);
                    success &= userDAO.placeOrder(productId, quantity);
                }

                if (success) {
                    JOptionPane.showMessageDialog(shoppingFrame, "Purchase successful!");
                    cartTableModel.setRowCount(0);  // Clear the cart
                    updateTotalCost(cartTableModel, totalCostLabel);
                    refreshProductTable();          // Refresh available products
                } else {
                    JOptionPane.showMessageDialog(shoppingFrame, "Some items could not be purchased.");
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });

        // Action Listener for employee access
        employeeButton.addActionListener(e -> showPasscodeDialog(shoppingFrame));

        // Action Listener for discounts
        discountButton.addActionListener(e -> showDiscountPopup(cartTableModel, totalCostLabel));
    }

    // Method to apply filters
    private void applyFilters(String name, String minPriceText, String maxPriceText) {
        double minPrice = minPriceText.isEmpty() ? Double.MIN_VALUE : Double.parseDouble(minPriceText);
        double maxPrice = maxPriceText.isEmpty() ? Double.MAX_VALUE : Double.parseDouble(maxPriceText);

        tableModel.setRowCount(0);
        try {
            List<Product> products = userDAO.getAllProducts();
            for (Product product : products) {
                if ((name.isEmpty() || product.getName().contains(name)) &&
                    product.getPrice() >= minPrice && product.getPrice() <= maxPrice) {
                    tableModel.addRow(new Object[]{
                            product.getId(), product.getName(), product.getQuantity(), product.getPrice()
                    });
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }


    // Function to show discount pop-up
    private void showDiscountPopup(DefaultTableModel cartTableModel, JLabel totalCostLabel) {
        JFrame discountFrame = new JFrame("Available Discounts");
        discountFrame.setSize(400, 300);
        discountFrame.setLocationRelativeTo(null);

        DefaultTableModel discountTableModel = new DefaultTableModel(new Object[]{"Product ID", "Discount", "Apply"}, 0);
        JTable discountTable = new JTable(discountTableModel);
        
        // Check for discounts based on conditions
        for (int i = 0; i < cartTableModel.getRowCount(); i++) {
            int productId = (int) cartTableModel.getValueAt(i, 0);
            int quantity = (int) cartTableModel.getValueAt(i, 2);
            double price = (double) cartTableModel.getValueAt(i, 3);
            
            // Check quantity-based discount
            if (quantity > 15) {
                discountTableModel.addRow(new Object[]{productId, "15% Discount (Quantity < 10)", "Apply"});
            }
            // Check price-based discount
            if (price > 1000) {
                discountTableModel.addRow(new Object[]{productId, "3% Discount (Price > 1000)", "Apply"});
            }
        }

        discountTable.getColumn("Apply").setCellRenderer(new ButtonRenderer());
        discountTable.getColumn("Apply").setCellEditor(new ButtonEditor(new JCheckBox()) {
            @Override
            public void fireEditingStopped() {
                int row = discountTable.getSelectedRow();
                int productId = (int) discountTableModel.getValueAt(row, 0);
                String discount = (String) discountTableModel.getValueAt(row, 1);

                // Apply discount
                for (int i = 0; i < cartTableModel.getRowCount(); i++) {
                    if ((int) cartTableModel.getValueAt(i, 0) == productId) {
                        double originalTotal = (double) cartTableModel.getValueAt(i, 4);
                        double discountAmount = discount.contains("15%") ? 0.15 : 0.03;
                        double newTotal = originalTotal * (1 - discountAmount);
                        cartTableModel.setValueAt(newTotal, i, 4);
                        updateTotalCost(cartTableModel, totalCostLabel);
                        break;
                    }
                }
                super.fireEditingStopped();
            }
        });

        discountFrame.add(new JScrollPane(discountTable));
        discountFrame.setVisible(true);
    }

    // Helper function to update total cost
    private void updateTotalCost(DefaultTableModel cartTableModel, JLabel totalCostLabel) {
        double totalCost = 0;
        for (int i = 0; i < cartTableModel.getRowCount(); i++) {
            totalCost += (double) cartTableModel.getValueAt(i, 4);
        }
        totalCostLabel.setText(String.format("Total: $%.2f", totalCost));
    }


    private JPanel createCartPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        cartTableModel = new DefaultTableModel(new Object[]{"Product ID", "Name", "Quantity", "Price", "Total Price"}, 0);
        JTable cartTable = new JTable(cartTableModel);
        refreshCartTable();

        JPanel checkoutPanel = new JPanel();
        JButton checkoutButton = new JButton("Place Order");

        checkoutButton.addActionListener(e -> {
            try {
                boolean success = placeOrderFromCart();
                if (success) {
                    JOptionPane.showMessageDialog(this, "Order placed successfully!");
                    shoppingCart.clear();
                    refreshCartTable();
                    refreshProductTable();
                } else {
                    JOptionPane.showMessageDialog(this, "Order could not be placed.");
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });

        checkoutPanel.add(checkoutButton);
        panel.add(new JScrollPane(cartTable), BorderLayout.CENTER);
        panel.add(checkoutPanel, BorderLayout.SOUTH);
        return panel;
    }

    private void refreshCartTable() {
        cartTableModel.setRowCount(0);
        for (ShoppingCartItem item : shoppingCart) {
            cartTableModel.addRow(new Object[]{
                item.getProductId(),
                item.getProductName(),
                item.getQuantity(),
                item.getPrice(),
                item.getTotalPrice()
            });
        }
    }

    private boolean placeOrderFromCart() throws SQLException {
        boolean allSuccessful = true;
        for (ShoppingCartItem item : shoppingCart) {
            boolean success = userDAO.placeOrder(item.getProductId(), item.getQuantity());
            if (!success) {
                allSuccessful = false;
            }
        }
        return allSuccessful;
    }

    // Refresh products in the table
//    private void refreshProductTable() {
//        tableModel.setRowCount(0);
//        try {
//            List<Product> products = userDAO.getAllProducts();
//            for (Product product : products) {
//                tableModel.addRow(new Object[]{product.getId(), product.getName(), product.getQuantity(), product.getPrice()});
//            }
//        } catch (SQLException ex) {
//            ex.printStackTrace();
//        }
//    }


    private void showPasscodeDialog(JFrame parentFrame) {
        JPasswordField passcodeField = new JPasswordField(15);
        JPanel panel = new JPanel(new GridLayout(2, 2, 5, 5));
        panel.add(new JLabel("Enter Passcode:"));
        panel.add(passcodeField);

        int option = JOptionPane.showConfirmDialog(parentFrame, panel, "Employee Access", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            String enteredPasscode = new String(passcodeField.getPassword());
            if ("1234".equals(enteredPasscode)) {
                showManagementPage();
            } else {
                JOptionPane.showMessageDialog(parentFrame, "Invalid passcode.");
            }
        }
    }

    private void showManagementPage() {
        JFrame managementFrame = new JFrame("Management Page");
        managementFrame.setSize(650, 550);
        managementFrame.setLocationRelativeTo(this);

        JTabbedPane tabbedPane = new JTabbedPane();

        tabbedPane.addTab("Add Product", createAddProductPanel());
        tabbedPane.addTab("Delete Product", createDeleteProductPanel());
        tabbedPane.addTab("Modify Product", createModifyProductPanel());

        tableModel = new DefaultTableModel(new Object[]{"Product ID", "Name", "Quantity", "Price"}, 0);
        productTable = new JTable(tableModel);
        refreshProductTable();

        managementFrame.add(new JScrollPane(productTable), BorderLayout.CENTER);
        managementFrame.add(tabbedPane, BorderLayout.SOUTH);
        managementFrame.setVisible(true);
    }

    private JPanel createAddProductPanel() {
        JPanel panel = new JPanel(new FlowLayout());
        JTextField nameField = new JTextField(10);
        JTextField quantityField = new JTextField(5);
        JTextField priceField = new JTextField(5);
        JButton addButton = new JButton("Add Product");

        panel.add(new JLabel("Name:"));
        panel.add(nameField);
        panel.add(new JLabel("Quantity:"));
        panel.add(quantityField);
        panel.add(new JLabel("Price:"));
        panel.add(priceField);
        panel.add(addButton);

        addButton.addActionListener(e -> {
            String name = nameField.getText();
            int quantity = Integer.parseInt(quantityField.getText());
            double price = Double.parseDouble(priceField.getText());
            try {
                boolean success = userDAO.addProduct(name, quantity, price);
                if (success) {
                    JOptionPane.showMessageDialog(this, "Product added successfully!");
                    refreshProductTable();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to add product.");
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });
        return panel;
    }

    private JPanel createDeleteProductPanel() {
        JPanel panel = new JPanel(new FlowLayout());
        JTextField productIdField = new JTextField(5);
        JButton deleteButton = new JButton("Delete Product");

        panel.add(new JLabel("Product ID:"));
        panel.add(productIdField);
        panel.add(deleteButton);

        deleteButton.addActionListener(e -> {
            int productId = Integer.parseInt(productIdField.getText());
            try {
                boolean success = userDAO.deleteProduct(productId);
                if (success) {
                    JOptionPane.showMessageDialog(this, "Product deleted successfully!");
                    refreshProductTable();
                } else {
                    JOptionPane.showMessageDialog(this, "Product not found.");
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });
        return panel;
    }

    private JPanel createModifyProductPanel() {
        JPanel panel = new JPanel(new FlowLayout());
        JTextField idField = new JTextField(5);
        JTextField nameField = new JTextField(10);
        JTextField quantityField = new JTextField(5);
        JTextField priceField = new JTextField(5);
        JButton modifyButton = new JButton("Modify Product");

        panel.add(new JLabel("Product ID:"));
        panel.add(idField);
        panel.add(new JLabel("New Name:"));
        panel.add(nameField);
        panel.add(new JLabel("New Quantity:"));
        panel.add(quantityField);
        panel.add(new JLabel("New Price:"));
        panel.add(priceField);
        panel.add(modifyButton);

        modifyButton.addActionListener(e -> {
            int id = Integer.parseInt(idField.getText());
            String name = nameField.getText();
            int quantity = Integer.parseInt(quantityField.getText());
            double price = Double.parseDouble(priceField.getText());
            try {
                boolean success = userDAO.modifyProduct(id, name, quantity, price);
                if (success) {
                    JOptionPane.showMessageDialog(this, "Product modified successfully!");
                    refreshProductTable();
                } else {
                    JOptionPane.showMessageDialog(this, "Product not found.");
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });
        return panel;
    }

    private void refreshProductTable() {
        tableModel.setRowCount(0);
        try {
            List<Product> products = userDAO.getAllProducts();
            for (Product product : products) {
                tableModel.addRow(new Object[]{product.getId(), product.getName(), product.getQuantity(), product.getPrice()});
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            UserAppGUI app = new UserAppGUI();
            app.setVisible(true);
        });
    }
}
