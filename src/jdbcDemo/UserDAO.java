package jdbcDemo;
import java.sql.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
public class UserDAO {
   private final String url = "jdbc:mysql://localhost:3306/user_management";
   private final String user = "root";
   private final String password = "KarthikArun07";
  
   public Connection getConnection() throws SQLException {
       return DriverManager.getConnection(url, user, password);
   }
   // Method to register a new user
   public boolean registerUser(String username, String plainPassword, String email) throws SQLException {
       String hashedPassword = hashPassword(plainPassword);
      
       String query = "INSERT INTO users (username, password, email) VALUES (?, ?, ?)";
       try (Connection con = DriverManager.getConnection(url, user, password);
            PreparedStatement pst = con.prepareStatement(query)) {
          
           pst.setString(1, username);
           pst.setString(2, hashedPassword);
           pst.setString(3, email);
           int rowsAffected = pst.executeUpdate();
           return rowsAffected > 0;  // true if registration successful
       }
   }
   // Method to authenticate user
   public boolean loginUser(String username, String plainPassword) throws SQLException {
       String query = "SELECT password FROM users WHERE username = ?";
      
       try (Connection con = DriverManager.getConnection(url, user, password);
            PreparedStatement pst = con.prepareStatement(query)) {
          
           pst.setString(1, username);
           ResultSet rs = pst.executeQuery();
           if (rs.next()) {
               String storedHashedPassword = rs.getString("password");
               return storedHashedPassword.equals(hashPassword(plainPassword));
           }
       }
       return false;  // Authentication failed
   }
   // Utility method to hash password using SHA-256
   private String hashPassword(String password) {
       try {
           MessageDigest md = MessageDigest.getInstance("SHA-256");
           byte[] hashedBytes = md.digest(password.getBytes());
           StringBuilder sb = new StringBuilder();
           for (byte b : hashedBytes) {
               sb.append(String.format("%02x", b));
           }
           return sb.toString();
       } catch (NoSuchAlgorithmException e) {
           throw new RuntimeException(e);  // Re-throw as unchecked exception
       }
   }
  
   public List<Product> getAllProducts() throws SQLException {
       String query = "SELECT * FROM products";
       List<Product> products = new ArrayList<>();
       try (Connection con = getConnection();
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(query)) {
           while (rs.next()) {
               int id = rs.getInt("product_id");
               String name = rs.getString("name");
               int quantity = rs.getInt("quantity");
               double price = rs.getDouble("price");
               products.add(new Product(id, name, quantity, price));
           }
       }
       return products;
   }
   // Method to process an order
   public boolean placeOrder(int productId, int orderQuantity) throws SQLException {
       String checkQuery = "SELECT quantity FROM products WHERE product_id = ?";
       String updateQuery = "UPDATE products SET quantity = quantity - ? WHERE product_id = ?";
      
       try (Connection con = DriverManager.getConnection(url, user, password)) {
           // Check product availability
           try (PreparedStatement checkStmt = con.prepareStatement(checkQuery)) {
               checkStmt.setInt(1, productId);
               ResultSet rs = checkStmt.executeQuery();
              
               if (rs.next()) {
                   int availableQuantity = rs.getInt("quantity");
                   if (availableQuantity < orderQuantity) {
                       System.out.println("Insufficient stock.");
                       return false;
                   }
               } else {
                   System.out.println("Product not found.");
                   return false;
               }
           }
          
           // Update product quantity if sufficient stock
           try (PreparedStatement updateStmt = con.prepareStatement(updateQuery)) {
               updateStmt.setInt(1, orderQuantity);
               updateStmt.setInt(2, productId);
              
               int rowsAffected = updateStmt.executeUpdate();
               return rowsAffected > 0;
           }
       }
   }
  
   // Method to add a product
   public boolean addProduct(String name, int quantity, double price) throws SQLException {
       String query = "INSERT INTO products (name, quantity, price) VALUES (?, ?, ?)";
      
       try (Connection con = DriverManager.getConnection(url, user, password);
            PreparedStatement pst = con.prepareStatement(query)) {
          
           pst.setString(1, name);
           pst.setInt(2, quantity);
           pst.setDouble(3, price);
          
           int rowsAffected = pst.executeUpdate();
           return rowsAffected > 0;
       }
   }
  
   // Method to delete a product
   public boolean deleteProduct(int productId) throws SQLException {
       String query = "DELETE FROM products WHERE product_id = ?";
      
       try (Connection con = DriverManager.getConnection(url, user, password);
            PreparedStatement pst = con.prepareStatement(query)) {
          
           pst.setInt(1, productId);
          
           int rowsAffected = pst.executeUpdate();
           return rowsAffected > 0;
       }
   }
  
   // Method to modify a product
   public boolean modifyProduct(int productId, String name, int quantity, double price) throws SQLException {
       String query = "UPDATE products SET name = ?, quantity = ?, price = ? WHERE product_id = ?";
      
       try (Connection con = DriverManager.getConnection(url, user, password);
            PreparedStatement pst = con.prepareStatement(query)) {
          
           pst.setString(1, name);
           pst.setInt(2, quantity);
           pst.setDouble(3, price);
           pst.setInt(4, productId);
          
           int rowsAffected = pst.executeUpdate();
           return rowsAffected > 0;
       }
   }
   
   public Product getProductById(int productId) throws SQLException {
	    String query = "SELECT * FROM products WHERE product_id = ?";
	    try (Connection con = getConnection();
	         PreparedStatement pst = con.prepareStatement(query)) {
	        pst.setInt(1, productId);
	        ResultSet rs = pst.executeQuery();
	        if (rs.next()) {
	            String name = rs.getString("name");
	            int quantity = rs.getInt("quantity");
	            double price = rs.getDouble("price");
	            return new Product(productId, name, quantity, price);
	        }
	    }
	    return null;  // Product not found
	}
   
   public List<Product> getProductsSortedByPrice(boolean ascending) throws SQLException {
       List<Product> products = new ArrayList<>();
       String sortOrder = ascending ? "ASC" : "DESC";
       String sql = "SELECT * FROM products ORDER BY price " + sortOrder;
       try (Connection con = getConnection();
    		     PreparedStatement pst = con.prepareStatement(sql)) {
           ResultSet rs = pst.executeQuery();
           while (rs.next()) {
               products.add(mapRowToProduct(rs));
           }
       }
       return products;
   }

   // 2. Filter Products by Minimum Quantity
   public List<Product> getProductsByMinimumQuantity(int minQuantity) throws SQLException {
       List<Product> products = new ArrayList<>();
       String sql = "SELECT * FROM products WHERE quantity > ?";
       try (Connection con = getConnection();
    		     PreparedStatement pst = con.prepareStatement(sql)) {
           pst.setInt(1, minQuantity);
           ResultSet rs = pst.executeQuery();
           while (rs.next()) {
               products.add(mapRowToProduct(rs));
           }
       }
       return products;
   }

   // Helper method to map ResultSet to Product object
   private Product mapRowToProduct(ResultSet rs) throws SQLException {
       return new Product(
               rs.getInt("id"),
               rs.getString("name"),
               rs.getInt("quantity"),
               rs.getDouble("price")
       );
   }

}
