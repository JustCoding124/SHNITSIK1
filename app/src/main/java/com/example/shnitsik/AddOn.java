package com.example.shnitsik;

public class AddOn {
    private String addOnName;
    private int amount=1;
    private int stock;
    private double pricePerOneAmount;
    public AddOn(){}
    public AddOn(String addOnName,double pricePerOneAmount,int stock){
        this.pricePerOneAmount = pricePerOneAmount;
        this.addOnName = addOnName;
        this.stock = stock;
    }
    public AddOn(String addOnName,int amount){
        this.addOnName = addOnName;
        this.amount = amount;
    }
    public double getTotalAddOnPrice(){
        return pricePerOneAmount * amount;
    }
    public String getAddOnName(){return this.addOnName;}
    public int getAmount(){return this.amount;}
    public int getStock(){return this.stock;}
    public double getPricePerOneAmount(){return this.pricePerOneAmount;}
    public void setAmount(int amount){this.amount = amount;}
    public boolean isSelected() {
        return amount > 0; // אם הכמות גדולה מ-0, נחשיב את התוספת כ"נבחרת"
    }
}

