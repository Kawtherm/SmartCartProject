package com.smartlife.smartcart.model;

import com.google.gson.Gson;

import java.io.Serializable;
import java.util.ArrayList;

public class Order implements Serializable {
    public String orderID;
    public String userID;
    public String customerNumber;
    public double customerBalance;
    public String customerID;
    public String invoiceDate;
    public int machineRecid;
    public String status;
    public double totalAmount;
    public String invoiceNo;
    public String storeNo;
    public String invoiceDetail;
    public String cartNo;
    public String companyId;
    public String shiftNo;
    public boolean isKNet;
    public boolean isVisa;
    public boolean isMasterCard;
    public String clearRecid;
    public String recid;

    public String getInvoiceDetailsForEEMC() {

        Gson gson = new Gson();
        Product[] invoiceDetail = gson.fromJson(this.invoiceDetail, Product[].class);

        ArrayList<Product.ProductEEMC> eemcDetails = new ArrayList<>();
        for (Product product : invoiceDetail) {
            Product.ProductEEMC p = product.getProductEEMC();
            //p.salesRecid = recid;
            p.itemsRecid = Integer.parseInt(product.id); //Integer.parseInt(recid);
            p.itemRecid = product.id;
            eemcDetails.add(p);
        }

        return gson.toJson(eemcDetails);
    }

    public String getInvoiceDetailsForIntegration() {

        Gson gson = new Gson();
        Product[] invoiceDetail = gson.fromJson(this.invoiceDetail, Product[].class);

        ArrayList<Product.ProductIntegration> products = new ArrayList<>();
        for (Product product : invoiceDetail) {
            products.add(product.getProductIntegration());
        }

        return gson.toJson(products);
    }
}
