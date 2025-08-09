package com.myproject;
import java.sql.*;
import java.util.*;

// ---------- Model Classes ----------
class FoodItem {
    private int id;
    private String name;
    private double price;

    public FoodItem(int id, String name, double price) {
        this.id = id;
        this.name = name;
        this.price = price;
    }
    public int getId() { return id; }
    public String getName() { return name; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    @Override
    public String toString() {
        return "FoodItem{id=" + id + ", name='" + name + "', price=" + price + "}";
    }
    @Override
    public int hashCode() { return Objects.hash(id); }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FoodItem)) return false;
        FoodItem that = (FoodItem) o;
        return id == that.id;
    }
}

class User {
    private int userId;
    private String username;
    private long contactNo;

    public User(int userId, String username, long contactNo) {
        this.userId = userId;
        this.username = username;
        this.contactNo = contactNo;
    }
    public int getUserId() { return userId; }
    public String getUsername() { return username; }
    public long getContactNo() { return contactNo; }
    @Override
    public String toString() { return username; }
}

class Customer extends User {
    private Cart cart;
    public Customer(int userId, String username, long contactNo) {
        super(userId, username, contactNo);
        this.cart = new Cart();
    }
    public Cart getCart() { return cart; }
}

class DeliveryPerson {
    private int deliveryPersonId;
    private String name;
    private long contactNo;
    public DeliveryPerson(int id, String name, long contactNo) {
        this.deliveryPersonId = id;
        this.name = name;
        this.contactNo = contactNo;
    }
    public int getId() { return deliveryPersonId; }
    @Override
    public String toString() { return name; }
}

class Restaurant {
    private int id;
    private String name;
    private List<FoodItem> menu = new ArrayList<>();
    public Restaurant(int id, String name) {
        this.id = id;
        this.name = name;
    }
    public int getId() { return id; }
    public String getName() { return name; }
    public List<FoodItem> getMenu() { return menu; }
    public void addFoodItem(FoodItem item) { menu.add(item); }
    public void removeFoodItem(int foodId) { menu.removeIf(f -> f.getId() == foodId); }
    @Override
    public String toString() {
        return "Restaurant ID: " + id + ", Name: " + name;
    }
}

class Cart {
    private Map<FoodItem, Integer> items = new HashMap<>();
    public void addItem(FoodItem item, int qty) {
        items.put(item, items.getOrDefault(item, 0) + qty);
    }
    public void removeItem(FoodItem item) {
        items.remove(item);
    }
    public Map<FoodItem, Integer> getItems() { return items; }
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Cart:\n");
        double total = 0;
        for (Map.Entry<FoodItem, Integer> entry : items.entrySet()) {
            double cost = entry.getKey().getPrice() * entry.getValue();
            sb.append("Food Item: ").append(entry.getKey().getName())
              .append(", Quantity: ").append(entry.getValue())
              .append(", Cost: Rs. ").append(cost).append("\n");
            total += cost;
        }
        sb.append("Total Cost: Rs. ").append(total);
        return sb.toString();
    }
}

class Order {
    private int orderId;
    private Customer customer;
    private Map<FoodItem, Integer> items;
    private String status = "Pending";
    private DeliveryPerson deliveryPerson;

    public Order(int id, Customer customer) {
        this.orderId = id;
        this.customer = customer;
        this.items = new HashMap<>(customer.getCart().getItems());
    }
    public int getOrderId() { return orderId; }
    public void assignDeliveryPerson(DeliveryPerson dp) { this.deliveryPerson = dp; }
    @Override
    public String toString() {
        return "Order{" +
                "orderId=" + orderId +
                ", customer=" + customer +
                ", items=" + items +
                ", status='" + status + '\'' +
                ", deliveryPerson=" + (deliveryPerson != null ? deliveryPerson : "Not Assigned") +
                '}';
    }
}

// ---------- Main Class with JDBC ----------
class JDBCExample {
    static final String URL = "jdbc:mysql://localhost:3306/fooddelivery";
    static final String USER = "root";
    static final String PASS = "Punam2004";

    public static void main(String[] args) {
        try (Connection con = DriverManager.getConnection(URL, USER, PASS);
             Scanner sc = new Scanner(System.in)) {

            Class.forName("com.mysql.cj.jdbc.Driver");

            while (true) {
                System.out.println("\n1. Admin Menu\n2. Customer Menu\n3. Exit");
                System.out.print("Choose an option: ");
                int choice = sc.nextInt();
                switch (choice) {
                    case 1 -> adminMenu(con, sc);
                    case 2 -> customerMenu(con, sc);
                    case 3 -> { System.out.println("Goodbye!"); return; }
                    default -> System.out.println("Invalid option");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ---------- Admin Menu with JDBC ----------
    private static void adminMenu(Connection con, Scanner sc) throws SQLException {
        while (true) {
            System.out.println("\nAdmin Menu:");
            System.out.println("1. Add Restaurant\n2. Add Food Item\n3. Remove Food Item\n4. View Menus\n5. View Orders\n6. Add Delivery Person\n7. Assign Delivery\n8. Exit");
            int ch = sc.nextInt();
            sc.nextLine();
            switch (ch) {
                case 1 -> {
                    System.out.print("Enter Restaurant ID: "); int id = sc.nextInt(); sc.nextLine();
                    System.out.print("Enter Name: "); String name = sc.nextLine();
                    PreparedStatement ps = con.prepareStatement("INSERT INTO Restaurant VALUES (?, ?)");
                    ps.setInt(1, id); ps.setString(2, name);
                    ps.executeUpdate();
                    System.out.println("Restaurant added!");
                }
                case 2 -> {
                    System.out.print("Enter Food ID: "); int fid = sc.nextInt(); sc.nextLine();
                    System.out.print("Enter Name: "); String fname = sc.nextLine();
                    System.out.print("Enter Price: "); double price = sc.nextDouble();
                    System.out.print("Enter Restaurant ID: "); int rid = sc.nextInt();
                    PreparedStatement ps = con.prepareStatement("INSERT INTO FoodItem VALUES (?, ?, ?, ?)");
                    ps.setInt(1, fid); ps.setString(2, fname); ps.setDouble(3, price); ps.setInt(4, rid);
                    ps.executeUpdate();
                    System.out.println("Food item added!");
                }
                case 3 -> {
                    System.out.print("Enter Food ID to remove: "); int fid = sc.nextInt();
                    PreparedStatement ps = con.prepareStatement("DELETE FROM FoodItem WHERE id=?");
                    ps.setInt(1, fid);
                    ps.executeUpdate();
                    System.out.println("Food item removed!");
                }
                case 4 -> {
                    Statement st = con.createStatement();
                    ResultSet rs = st.executeQuery("SELECT r.id, r.name, f.id, f.name, f.price FROM Restaurant r LEFT JOIN FoodItem f ON r.id=f.restaurant_id");
                    int currentRest = -1;
                    while (rs.next()) {
                        if (currentRest != rs.getInt(1)) {
                            currentRest = rs.getInt(1);
                            System.out.println("\nRestaurant ID: " + rs.getInt(1) + ", Name: " + rs.getString(2));
                        }
                        if (rs.getInt(3) != 0) {
                            System.out.println("  - Food ID: " + rs.getInt(3) + ", " + rs.getString(4) + ", Rs." + rs.getDouble(5));
                        }
                    }
                }
                case 5 -> {
                    Statement st = con.createStatement();
                    ResultSet rs = st.executeQuery("SELECT o.orderId, c.username, o.status, d.name FROM Orders o JOIN Customer c ON o.customerId=c.userId LEFT JOIN DeliveryPerson d ON o.deliveryPersonId=d.deliveryPersonId");
                    while (rs.next()) {
                        System.out.println("Order ID: " + rs.getInt(1) + ", Customer: " + rs.getString(2) + ", Status: " + rs.getString(3) + ", Delivery: " + (rs.getString(4) != null ? rs.getString(4) : "Not Assigned"));
                    }
                }
                case 6 -> {
                    System.out.print("Enter Delivery Person ID: "); int id = sc.nextInt(); sc.nextLine();
                    System.out.print("Enter Name: "); String name = sc.nextLine();
                    System.out.print("Enter Contact No: "); long phone = sc.nextLong();
                    PreparedStatement ps = con.prepareStatement("INSERT INTO DeliveryPerson VALUES (?, ?, ?)");
                    ps.setInt(1, id); ps.setString(2, name); ps.setLong(3, phone);
                    ps.executeUpdate();
                    System.out.println("Delivery person added!");
                }
                case 7 -> {
                    System.out.print("Enter Order ID: "); int oid = sc.nextInt();
                    System.out.print("Enter Delivery Person ID: "); int did = sc.nextInt();
                    PreparedStatement ps = con.prepareStatement("UPDATE Orders SET deliveryPersonId = ? WHERE orderId = ?");
                    ps.setInt(1, did); ps.setInt(2, oid);
                    ps.executeUpdate();
                    System.out.println("Assigned successfully!");
                }
                case 8 -> { return; }
            }
        }
    }

    // ---------- Customer Menu with JDBC ----------
    private static void customerMenu(Connection con, Scanner sc) throws SQLException {
        while (true) {
            System.out.println("\nCustomer Menu:");
            System.out.println("1. Add Customer\n2. View Food Items\n3. Place Order\n4. View Orders\n5. Exit");
            int ch = sc.nextInt();
            sc.nextLine();
            switch (ch) {
                case 1 -> {
                    System.out.print("Enter Customer ID: "); int id = sc.nextInt(); sc.nextLine();
                    System.out.print("Enter Name: "); String name = sc.nextLine();
                    System.out.print("Enter Contact No: "); long phone = sc.nextLong();
                    PreparedStatement ps = con.prepareStatement("INSERT INTO Customer VALUES (?, ?, ?)");
                    ps.setInt(1, id); ps.setString(2, name); ps.setLong(3, phone);
                    ps.executeUpdate();
                    System.out.println("Customer created!");
                }
                case 2 -> {
                    Statement st = con.createStatement();
                    ResultSet rs = st.executeQuery("SELECT f.id, f.name, f.price, r.name FROM FoodItem f JOIN Restaurant r ON f.restaurant_id = r.id");
                    while (rs.next()) {
                        System.out.println("Food ID: " + rs.getInt(1) + ", " + rs.getString(2) + ", Rs." + rs.getDouble(3) + " (Restaurant: " + rs.getString(4) + ")");
                    }
                }
                case 3 -> {
                    System.out.print("Enter Customer ID: "); int cid = sc.nextInt();
                    System.out.print("Enter Food ID: "); int fid = sc.nextInt();
                    System.out.print("Enter Quantity: "); int qty = sc.nextInt();
                    PreparedStatement psOrder = con.prepareStatement("INSERT INTO Orders (customerId) VALUES (?)", Statement.RETURN_GENERATED_KEYS);
                    psOrder.setInt(1, cid);
                    psOrder.executeUpdate();
                    ResultSet keys = psOrder.getGeneratedKeys();
                    if (keys.next()) {
                        int orderId = keys.getInt(1);
                        PreparedStatement psItem = con.prepareStatement("INSERT INTO OrderItems VALUES (?, ?, ?)");
                        psItem.setInt(1, orderId);
                        psItem.setInt(2, fid);
                        psItem.setInt(3, qty);
                        psItem.executeUpdate();
                        System.out.println("Order placed! ID: " + orderId);
                    }
                }
                case 4 -> {
                    System.out.print("Enter Customer ID: "); int cid = sc.nextInt();
                    PreparedStatement ps = con.prepareStatement("SELECT o.orderId, o.status, f.name, oi.quantity FROM Orders o JOIN OrderItems oi ON o.orderId=oi.orderId JOIN FoodItem f ON oi.foodItemId=f.id WHERE o.customerId=?");
                    ps.setInt(1, cid);
                    ResultSet rs = ps.executeQuery();
                    while (rs.next()) {
                        System.out.println("Order ID: " + rs.getInt(1) + ", Status: " + rs.getString(2) + ", Item: " + rs.getString(3) + ", Qty: " + rs.getInt(4));
                    }
                }
                case 5 -> { return; }
            }
        }
    }
}
