package db;

import util.CustomerTM;
import util.ItemTM;

import java.util.ArrayList;

public class DB {

    public static ArrayList<CustomerTM> customers = new ArrayList<>();
    public static ArrayList<ItemTM> items = new ArrayList<>();
    public static ArrayList<Order> orders = new ArrayList<>();

    static{
        customers.add(new CustomerTM("C001","Kasun","Galle"));
        customers.add(new CustomerTM("C002","Nuwan","Matara"));
        customers.add(new CustomerTM("C003","Ruwan","Panadura"));
        customers.add(new CustomerTM("C004","Nipun","Kaluthara"));

        items.add(new ItemTM("I001","Mouse Pad",50,100));
        items.add(new ItemTM("I002","Mouse",20,300));
        items.add(new ItemTM("I003","Pen Drive",80,2500));
        items.add(new ItemTM("I004","Monitor",40,8000));
    }

}
