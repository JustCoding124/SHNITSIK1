package com.example.shnitsik.models;

/**
 * The type Add on.
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

