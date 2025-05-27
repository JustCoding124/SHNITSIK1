package com.example.shnitsik.models;

/**
 * Represents an individual add-on item that can be offered alongside a primary product or service.
 * This class encapsulates the essential details of an add-on, allowing for its management
 * within a larger system, such as an e-commerce platform or a point-of-sale application.
 *
 * The core purpose of this class is to model an optional item that a customer might choose
 * to include with their main purchase. For example, in a food ordering system, an "AddOn"
 * could represent "Extra Cheese" for a pizza or "French Fries" as a side for a burger.
 *
 * Key attributes of an AddOn include its name, the quantity desired, and the price for a single unit.
 *
 * **Variable Analysis:**
 *
 * - {@code private String addOnName;}:
 *   This field stores the descriptive name of the add-on. It's a crucial identifier for the user
 *   and for internal tracking. For instance, "Extra Pickles", "Gift Wrapping", or "Extended Warranty".
 *   The {@code String} type is appropriate for textual representation. It's declared as {@code private}
 *   to enforce encapsulation, meaning its direct modification from outside the class is prevented,
 *   and access is typically managed through getter methods.
 *
 * - {@code private int amount = 1;}:
 *   This integer variable represents the quantity of this specific add-on that has been selected or
 *   is being considered. It's initialized to {@code 1} by default, implying that if an add-on is
 *   created, it's typically with an initial quantity of one. This is a common scenario, but the
 *   quantity can be adjusted later using the {@code setAmount} method. For example, a customer might
 *   want 2 portions of "Extra Sauce". The {@code int} type is suitable for whole number quantities.
 *   Like {@code addOnName}, it's {@code private} for encapsulation.
 *
 * - {@code private double pricePerOneAmount;}:
 *   This field holds the monetary cost for a single unit of this add-on. For example, if "Extra Cheese"
 *   costs $0.50, this variable would store {@code 0.5}. The {@code double} type is used to accommodate
 */
public class AddOn {
    private String addOnName;
    private int amount=1;
    private double pricePerOneAmount;

    /**
     * Instantiates a new Add on.
     */
    public AddOn(){}

    /**
     * Instantiates a new Add on.
     *
     * @param addOnName         the add on name
     * @param pricePerOneAmount the price per one amount
     */
    public AddOn(String addOnName,double pricePerOneAmount){
        this.pricePerOneAmount = pricePerOneAmount;
        this.addOnName = addOnName;
    }

    /**
     * Instantiates a new Add on.
     *
     * @param addOnName the add on name
     * @param amount    the amount
     */
    public AddOn(String addOnName,int amount){
        this.addOnName = addOnName;
        this.amount = amount;
    }

    /**
     * Get total add on price double.
     *
     * @return the double
     */
    public double getTotalAddOnPrice(){
        return pricePerOneAmount * amount;
    }

    /**
     * Get add on name string.
     *
     * @return the string
     */
    public String getAddOnName(){return this.addOnName;}

    /**
     * Get amount int.
     *
     * @return the int
     */
    public int getAmount(){return this.amount;}

    /**
     * Get price per one amount double.
     *
     * @return the double
     */
    public double getPricePerOneAmount(){return this.pricePerOneAmount;}

    /**
     * Set amount.
     *
     * @param amount the amount
     */
    public void setAmount(int amount){this.amount = amount;}

    /**
     * Is selected boolean.
     *
     * @return the boolean
     */
    public boolean isSelected() {
        return amount > 0; // אם הכמות גדולה מ-0, נחשיב את התוספת כ"נבחרת"
    }
}

