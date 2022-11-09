package com.smartlife.smartcart.model;

import com.smartlife.smartcart.classes.AppConsts;

import java.util.ArrayList;

public class Product {

    public String name;
    public double price;
    public String barcode;
    public String originalBarcode;
    public ArrayList<Integer> weights;
    public int server_weight;
    public int quantity;
    public String id;
    public String storeNumber;
    public String iucID;
    public String packageID;
    public String supplierNumber;
    public String englishName;
    public String arabicName;
    public double cost;
    public boolean isPrefix = false;

    public Product() {
        this.quantity = 1;
        this.weights = new ArrayList<>();
    }

    public Double getSubtotal() {
        return price * quantity;
    }

    public ProductEEMC getProductEEMC() {
        return new ProductEEMC(this.barcode, this.price, this.quantity, this.id);
    }

    public ProductIntegration getProductIntegration() {
        return new ProductIntegration(this.barcode, this.price, this.cost, this.storeNumber, this.iucID, this.packageID, this.supplierNumber, this.englishName, this.arabicName, this.id, this.quantity);
    }

    public static class ProductEEMC {

        public final String barecode;
        public final double itemPrice;
        public final int itemQty;
        public  String itemRecid;
        public final int lineNo = 1;
        public final int promotion = 0;
        public final int discountAmount = 0;
        public final int discountPercent = 0;
        public final int status = 1;
        public final String userRecid = AppConsts.USER_REC_ID;
        public  long itemsRecid;
        public String salesRecid;

        public ProductEEMC(String barcode, double price, int count, String id) {
            this.barecode = barcode;
            this.itemPrice = price;
            this.itemQty = count;
            this.itemRecid = id;
        }
    }

    public static class ProductIntegration {

        public final int quantity;
        public final ProductDetail product;

        public ProductIntegration(String barcode, double price, double cost, String storeNumber, String iucID, String packageID, String supplier, String englishName, String arabicName, String id, int count) {
            this.quantity = count;
            this.product = new ProductDetail(barcode, price, cost, storeNumber, iucID, packageID, supplier, englishName, arabicName, id);
        }

        public static class ProductDetail {

            public final String barcode;
            public final String itemID;
            public final double price;
            public double cost;
            public String storeNumber;
            public String iucID;
            public String packageID;
            public String supplierNumber;
            public String englishName;
            public String arabicName;

            public ProductDetail(String barcode, double price, double cost, String storeNumber, String iucID, String packageID, String supplier, String englishName, String arabicName, String id) {
                this.barcode = barcode;
                this.itemID = id;
                this.price = price;
                this.cost = cost;
                this.storeNumber = storeNumber;
                this.iucID = iucID;
                this.packageID = packageID;
                this.supplierNumber = supplier;
                this.englishName = englishName;
                this.arabicName = arabicName;
            }
        }
    }
}
