package com.example.grocery.activities;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.grocery.R;
import com.example.grocery.adapters.AdapterCartItem;
import com.example.grocery.adapters.AdapterProductUser;
import com.example.grocery.models.ModelCartItem;
import com.example.grocery.models.ModelProduct;
import com.google.android.gms.common.internal.Constants;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.example.grocery.constants;

import java.util.ArrayList;
import java.util.HashMap;

import p32929.androideasysql_library.Column;
import p32929.androideasysql_library.EasyDB;

public class ShopDetailsActivity extends AppCompatActivity {
//declare ui views
private ImageView shopIv;
    private TextView  shopNameTv,phoneTv,emailTv,openClosedTv,deliveryFeeTv,addressTv,filteredProductsTv,cartCountTv;
    private ImageButton callBtn,mapBtn,cartBtn,backBtn,filterProductBtn;
    private EditText searchProductEt;
    private RecyclerView productsRv;
    private String shopUid;
    private FirebaseAuth firebaseAuth;
    private ArrayList<ModelProduct>productsList;
    private AdapterProductUser adapterProductUser;
private ProgressDialog progressDialog;
    private String myLatitude,myLongitude,myPhone;
    private String shopName,shopEmail,shopPhone,shopAddress,shopLatitude,shopLongitude;
    public String deliveryFee;

    //cart
    public ArrayList<ModelCartItem> cartItemList;
    private AdapterCartItem adapterCartItem;

   private EasyDB easyDB;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_details);

        //init ui views
        shopIv=findViewById(R.id.shopIv);
        filterProductBtn=findViewById(R.id.filterProductBtn);
        shopNameTv=findViewById(R.id.shopNameTv);
        mapBtn=findViewById(R.id.mapBtn);
        phoneTv=findViewById(R.id.phoneTv);
        cartBtn=findViewById(R.id.cartBtn);
        emailTv=findViewById(R.id.emailTv);
        openClosedTv=findViewById(R.id.openClosedTv);
        deliveryFeeTv=findViewById(R.id.deliveryFeeTv);
        addressTv=findViewById(R.id.addressTv);
        filteredProductsTv=findViewById(R.id.filteredProductsTv);
        searchProductEt=findViewById(R.id.searchProductEt);
        backBtn=findViewById(R.id.backBtn);
        callBtn=findViewById(R.id.callBtn);
        productsRv =findViewById(R.id.productsRv);
        cartCountTv =findViewById(R.id.cartCountTv);
        //init progress dialog
        progressDialog=new ProgressDialog(this);
        progressDialog.setTitle("PLEASE WAIT");
        progressDialog.setCanceledOnTouchOutside(false);


        //get uid of the shop from intent
         shopUid=getIntent().getStringExtra("shopUid");
         firebaseAuth=FirebaseAuth.getInstance();
         loadMyInfo();
         loadShopDetails();
         loadShopProducts();
//declare it to class level and init it oncreate
         easyDB = EasyDB.init(this,"ITEMS_DB")
                .setTableName("ITEMS_TABLE")
                .addColumn(new Column("Item_Id",new String[]{"text","unique"}))
                .addColumn(new Column("Item_PId",new String[]{"text","not null"}))
                .addColumn(new Column("Item_Name",new String[]{"text","not null"}))
                .addColumn(new Column("Item_Price_Each",new String[]{"text","not null"}))
                .addColumn(new Column("Item_Price",new String[]{"text","not null"}))
                .addColumn(new Column("Item_Quantity",new String[]{"text","not null"}))
                .doneTableColumn();

//each shop has its own products and orders so if user add item to cart and open cart in different shop then cart should be different
        //so delete cart data whenever user open this activity

         deleteCartData();
        cartCount();

         //search
        searchProductEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try{
                adapterProductUser.getFilter().filter(s);
                }
                catch (Exception e){
                e.printStackTrace();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //go to previous
                onBackPressed();
            }
        });
        cartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //show cart dialog
                showCartDialog();
                

            }
        });
        callBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialPhone();
            }
        });
        mapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openMap();
            }
        });
        filterProductBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder=new AlertDialog.Builder(ShopDetailsActivity.this);
                builder.setTitle("Filtered Products:")
                        .setItems(constants.productCategories1, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //get selected item
                                String selected=constants.productCategories1[which];
                                filteredProductsTv.setText(selected);
                                if(selected.equals("All")){
                                    //load all
                                    loadShopProducts();
                                }
                                else{
                                    //load filtered
                                    adapterProductUser.getFilter().filter(selected);
                                }
                            }
                        }).show();
            }
        });
    }

    private void deleteCartData() {

        easyDB.deleteAllDataFromTable();//delete all records from cart

    }
public void cartCount(){
        //keep it public so that we can access in adapter
    //get cart count
    int count=easyDB.getAllData().getCount();
    if(count<=0){
        //no itenm in cart ,hide cart count textview
    cartCountTv.setVisibility(View.GONE);
    }
    else{
        //have items in cart,show cart textview and set count
        cartCountTv.setVisibility(View.VISIBLE);
        cartCountTv.setText(""+count);//conncatenate with string beacz we cant set integer in textview
    }
}
    public double allTotalPrice=0.00;
    //need to access these views in adapter so making public
    public TextView dFeeTv,sTotalTv,allTotalPriceTv;
    private void showCartDialog() {
        //init list
        cartItemList =new ArrayList<>();
        //inflate cart layout
        View view= LayoutInflater.from(this).inflate(R.layout.dialog_cart,null);
        //init views
TextView shopNameTv=view.findViewById(R.id.shopNameTv);
RecyclerView cartItemsIv=view.findViewById(R.id.cartItemsIv);
sTotalTv=view.findViewById(R.id.sTotalTv);
       dFeeTv=view.findViewById(R.id.dFeeTv);
        allTotalPriceTv=view.findViewById(R.id.totalTv);
        Button checkoutBtn=view.findViewById(R.id.checkoutBtn);

        //DIALOG
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        //set view to dialog
        builder.setView(view);
        shopNameTv.setText(shopName);
        EasyDB easyDB = EasyDB.init(this,"ITEMS_DB")
                .setTableName("ITEMS_TABLE")
                .addColumn(new Column("Item_Id",new String[]{"text","unique"}))
                .addColumn(new Column("Item_PId",new String[]{"text","not null"}))
                .addColumn(new Column("Item_Name",new String[]{"text","not null"}))
                .addColumn(new Column("Item_Price_Each",new String[]{"text","not null"}))
                .addColumn(new Column("Item_Price",new String[]{"text","not null"}))
                .addColumn(new Column("Item_Quantity",new String[]{"text","not null"}))
                .doneTableColumn();
        //get all records from db
        Cursor res =easyDB.getAllData();
        while (res.moveToLast()){
            String id=res.getString(1);
            String pId=res.getString(2);
            String name=res.getString(3);
            String price=res.getString(4);
            String cost=res.getString(5);
            String quantity=res.getString(6);
            allTotalPrice=allTotalPrice+Double.parseDouble(cost);
            ModelCartItem modelCartItem=new ModelCartItem(""+id,
                    ""+pId,
                    ""+name,
                    ""+price,
                    ""+cost,
                    ""+quantity);
            cartItemList.add(modelCartItem);
        }
        //setuip adapter
        adapterCartItem =new AdapterCartItem(this,cartItemList);
        //set to recyclerview
        cartItemsIv.setAdapter(adapterCartItem);
        dFeeTv.setText("$"+deliveryFee);
        sTotalTv.setText("$"+String.format("%.2f",allTotalPrice));
        allTotalPriceTv.setText("$"+(allTotalPrice+Double.parseDouble(deliveryFee.replace("$",""))));
        //show dialog
        AlertDialog dialog=builder.create();
        dialog.show();
       // reset total price on dialog dismiss
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                allTotalPrice=0.00;

            }
        });
        //place ordder
        checkoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //first validate delivery address
                if (myLatitude.equals("") || myLatitude.equals("null") || myLongitude.equals("") || myLongitude.equals("null")) {
                    //user didnt enter address in profile
                    Toast.makeText(ShopDetailsActivity.this, "please enter your address in profile before placing order", Toast.LENGTH_SHORT).show();
                    return;//dont proceed further

                }
                if (myPhone.equals("") || myPhone.equals("null")) {
                    //user didnt enter phone number in profile
                    Toast.makeText(ShopDetailsActivity.this, "please enter your phone number  in profile before placing order", Toast.LENGTH_SHORT).show();
                    return;//dont proceed further
                }
                if(cartItemList.size()==0){
                    //cart item empty
                    Toast.makeText(ShopDetailsActivity.this,"No items in cart",Toast.LENGTH_SHORT).show();
                    return;//dont proceed further
                }
                submitOrder();
            }
        });


    }

    private void submitOrder() {
//show progress dialog
progressDialog.setTitle("placing order...");
progressDialog.show();
//for order id and order time
final String timeStamp=""+System.currentTimeMillis();
    String cost=allTotalPriceTv.getText().toString().trim().replace("$","");
    //setup order data
    HashMap<String,String>hashMap=new HashMap<>();
        hashMap.put("orderId",""+timeStamp);
        hashMap.put("orderTime",""+timeStamp);
        hashMap.put("orderStatus",""+"In Progress");//in progess completed/camcelled
        hashMap.put("orderBy",""+firebaseAuth.getUid());
        hashMap.put("orderTo",""+shopUid);
        hashMap.put("orderCost",""+cost);

        //add to db
        final DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Users").child(shopUid).child("Orders");
        ref.child(timeStamp).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
//order info added ,now add order items
                        for(int i=0;i<cartItemList.size();i++){
                            String pId=cartItemList.get(i).getpId();
                            String id=cartItemList.get(i).getId();
                            String cost=cartItemList.get(i).getCost();
                            String price=cartItemList.get(i).getPrice();
                            String quantity=cartItemList.get(i).getQuantity();
                            String name=cartItemList.get(i).getName();

                            HashMap<String,String>hashMap1=new HashMap<>();
                            hashMap1.put("pId",pId);
                            hashMap1.put("name",name);
                            hashMap1.put("cost",cost);
                            hashMap1.put("price",price);
                            hashMap1.put("quantity",quantity);

                            ref.child(timeStamp).child("Items").child(pId).setValue(hashMap1);

                        }
                        progressDialog.dismiss();
                        Toast.makeText(ShopDetailsActivity.this,"Order Placed Successfully",Toast.LENGTH_SHORT).show();


                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //failed placing order
                progressDialog.dismiss();
                Toast.makeText(ShopDetailsActivity.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void openMap() {
        //saddr means source address
        //daddr means destination address
       String address="https//:maps.google.com/maps?saddr="+myLatitude+","+myLongitude+"&daddr="+shopLatitude+","+shopLongitude;
       Intent intent=new Intent(Intent.ACTION_VIEW,Uri.parse(address));
       startActivity(intent);
    }

    private void dialPhone() {
        startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:"+Uri.encode(shopPhone))));
        Toast.makeText(this,""+shopPhone,Toast.LENGTH_SHORT).show();
    }


    private void loadMyInfo() {
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Users");
        ref.orderByChild("uid").equalTo(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for(DataSnapshot ds:dataSnapshot.getChildren()){
                            //get user data

                            String name=""+ds.child("name").getValue();
                            String email=""+ds.child("email").getValue();
                            myPhone=""+ds.child("phone").getValue();
                            String profileImage=""+ds.child("profileImage").getValue();
                            String accountType=""+ds.child("accountType").getValue();
                           myLatitude=""+ds.child("latitude").getValue();
                            myLongitude=""+ds.child("longitude").getValue();

                        }
                    }



                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
    private void loadShopProducts() {
        //init list
productsList=new ArrayList<>();
DatabaseReference reference=FirebaseDatabase.getInstance().getReference("Users");
reference.child(shopUid).child("Products")
        .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //clear lists nefore adding items
                productsList.clear();
                for(DataSnapshot ds:dataSnapshot.getChildren()){
                    ModelProduct modelProduct=ds.getValue(ModelProduct.class);
                    productsList.add(modelProduct);
                }
                //setup adapter
                adapterProductUser =new AdapterProductUser(ShopDetailsActivity.this,productsList);
                //set adapter
                productsRv.setAdapter(adapterProductUser);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }


    private void loadShopDetails() {
        DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Users");
        ref.child(shopUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //get shop data
                String name=""+dataSnapshot.child("name").getValue();
                shopName=""+dataSnapshot.child("shopName").getValue();
                shopEmail=""+dataSnapshot.child("email").getValue();
                shopLatitude=""+dataSnapshot.child("Latitude").getValue();
                shopPhone=""+dataSnapshot.child("phone").getValue();
                shopAddress=""+dataSnapshot.child("address").getValue();
                shopLongitude=""+dataSnapshot.child("Longitude").getValue();
                 deliveryFee=""+dataSnapshot.child("deliveryFee").getValue();
                String profileImage=""+dataSnapshot.child("profileImage").getValue();
                String shopOpen=""+dataSnapshot.child("shopOpen").getValue();
                //set data
                shopNameTv.setText(shopName);
                emailTv.setText(shopEmail);
                deliveryFeeTv.setText("Delivery Fee: $"+deliveryFee);
                addressTv.setText(shopAddress);
                phoneTv.setText(shopPhone);
                if(shopOpen.equals("true")){
                    openClosedTv.setText("Open");
                }
                else{
                    openClosedTv.setText("Closed");
                }
                try{
Picasso.get().load(profileImage).into(shopIv);
                }
                catch (Exception e){

                }


                
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}