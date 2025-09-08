package com.bll.loanmanagementsystem;

import static android.app.ProgressDialog.show;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

public class LoanPay extends AppCompatActivity {
    DatabaseHelper dbHelper;
    MaterialToolbar BackIcon;
    TextView DetailsBankNameTv, DetailsLoanTv, DetailsLoanPayTv, DetailsLoanRemainingTv, DetailsLoanNoteTv, DetailsLoanDateTv, NoDataFoundTv;
    Button LoanPayAlertBtn;
    RecyclerView PaymentRecyclerView;
    private String loan_id;
    HashMap<String,String> hashMap;
    ArrayList<HashMap<String, String>> arrayList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_loan_pay);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        dbHelper = new DatabaseHelper(LoanPay.this);
        BackIcon = findViewById(R.id.BackIcon);
        DetailsBankNameTv = findViewById(R.id.DetailsBankNameTv);
        DetailsLoanTv = findViewById(R.id.DetailsLoanTv);
        DetailsLoanPayTv = findViewById(R.id.DetailsLoanPayTv);
        DetailsLoanRemainingTv = findViewById(R.id.DetailsLoanRemainingTv);
        DetailsLoanNoteTv = findViewById(R.id.DetailsLoanNoteTv);
        DetailsLoanDateTv = findViewById(R.id.DetailsLoanDateTv);
        LoanPayAlertBtn = findViewById(R.id.LoanPayAlertBtn);
        NoDataFoundTv = findViewById(R.id.NoDataFoundTv);
        PaymentRecyclerView = findViewById(R.id.PaymentRecyclerView);

        loan_id = getIntent().getStringExtra("loan_id");
        DetailsRefresh();
        LoadData();

        //=============== Back Page Button ===============//
        BackIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                finish();

            }
        });

        LoanPayAlertBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder alert = new AlertDialog.Builder(LoanPay.this);

                View myView = getLayoutInflater().inflate(R.layout.add_loan_pay, null);
                alert.setView(myView);

                AlertDialog dialog = alert.create();
                dialog.setCancelable(true);

                TextInputEditText edLoanPayAmount = myView.findViewById(R.id.edLoanPayAmount);
                TextInputEditText edLoanPayNote = myView.findViewById(R.id.edLoanPayNote);
                TextInputEditText edLoanPayDate = myView.findViewById(R.id.edLoanPayDate);
                Button PayBtn = myView.findViewById(R.id.PayBtn);


                //=============== Date Picker ===============
                SimpleDateFormat sdf = new SimpleDateFormat("dd-MMMM-yyyy", Locale.getDefault());
                String todayDate = sdf.format(Calendar.getInstance().getTime());
                edLoanPayDate.setText(todayDate);

                edLoanPayDate.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Calendar calendar = Calendar.getInstance();

                        DatePickerDialog datePickerDialog = new DatePickerDialog(LoanPay.this,
                                new DatePickerDialog.OnDateSetListener() {
                                    @Override
                                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                                        Calendar selectedCalendar = Calendar.getInstance();
                                        selectedCalendar.set(year, month, dayOfMonth);

                                        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMMM-yyyy", Locale.getDefault());
                                        String selectedDate = sdf.format(selectedCalendar.getTime());
                                        edLoanPayDate.setText(selectedDate);
                                    }
                                },
                                calendar.get(Calendar.YEAR),
                                calendar.get(Calendar.MONTH),
                                calendar.get(Calendar.DAY_OF_MONTH)
                        );

                        datePickerDialog.show();
                    }
                });

                PayBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        String sAmount = edLoanPayAmount.getText().toString().trim();
                        String note = edLoanPayNote.getText().toString().trim();
                        String date = edLoanPayDate.getText().toString().trim();

                        if (TextUtils.isEmpty(edLoanPayAmount.getText().toString())){
                            edLoanPayAmount.setError("Required");
                            return;
                        }


                        Double amount = Double.parseDouble(sAmount);
                        dbHelper.LoanPay(loan_id, amount, note, date);
                        dialog.dismiss();
                        DetailsRefresh();
                        LoadData();
                        Toast.makeText(getApplicationContext(), "Pay Successful"), Toast.LENGTH_SHORT).show();

                    }
                });

                dialog.show();

            }
        });

        //===============================================================================


    }

    //=============== Details Refresh ===============
    private void DetailsRefresh(){

        Cursor cursor = dbHelper.ShowSingleLoanDetails(loan_id);

        if (cursor!=null && cursor.getCount()>0){

            while (cursor.moveToNext()){

                String name = cursor.getString(1);
                double total_loan = cursor.getDouble(2);
                String note = cursor.getString(3);
                String date = cursor.getString(4);
                double total_paid = cursor.getDouble(5);
                double remaining_loan = cursor.getDouble(6);

                DetailsBankNameTv.setText(name);
                DetailsLoanTv.setText(": Tk" + " " + String.format("%,.0f", total_loan));
                DetailsLoanPayTv.setText(": Tk" + String.format("%,.0f", total_paid));
                DetailsLoanRemainingTv.setText(": Tk" + " " + String.format("%,.0f", remaining_loan));
                setTextOrHide(DetailsLoanNoteTv, note);
                DetailsLoanDateTv.setText(date);


            }

        }

    }

    //=============== Load Data ===============//
    private void LoadData() {

        Cursor cursor = dbHelper.ShowPaymentData(loan_id);
        if (cursor!=null && cursor.getCount()>0 ){

            arrayList = new ArrayList<>();

            while (cursor.moveToNext()){

                int id = cursor.getInt(0);
                double amount = cursor.getDouble(2);
                String note = cursor.getString(3);
                String date = cursor.getString(4);

                hashMap = new HashMap<>();
                hashMap.put("id", ""+id);
                hashMap.put("amount", ""+amount);
                hashMap.put("note", ""+note);
                hashMap.put("date", ""+date);
                arrayList.add(hashMap);

            }

            NoDataFoundTv.setVisibility(View.GONE);
            LoanPaymentAdapter adapter = new LoanPaymentAdapter();
            PaymentRecyclerView.setAdapter(adapter);
            PaymentRecyclerView.setLayoutManager(new LinearLayoutManager(LoanPay.this));

        }else {

            NoDataFoundTv.setVisibility(View.VISIBLE);

        }

    }

    //=============== Adapter ===============//
    private class LoanPaymentAdapter extends RecyclerView.Adapter <LoanPaymentAdapter.myViewHolder> {

        private class myViewHolder extends RecyclerView.ViewHolder{
            TextView PayTranAmountTv, PayTranNoteTv, PayTranDateTv;
            LinearLayout LoanPayDeleteBtn;

            public myViewHolder(@NonNull View itemView) {
                super(itemView);

                PayTranAmountTv = itemView.findViewById(R.id.PayTranAmountTv);
                PayTranNoteTv = itemView.findViewById(R.id.PayTranNoteTv);
                PayTranDateTv = itemView.findViewById(R.id.PayTranDateTv);
                LoanPayDeleteBtn = itemView.findViewById(R.id.LoanPayDeleteBtn);

            }
        }

        @NonNull
        @Override
        public myViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

            LayoutInflater inflater = getLayoutInflater();
            View myView = inflater.inflate(R.layout.display_loan_pay, parent, false);



            return new myViewHolder(myView);
        }

        @Override
        public void onBindViewHolder(@NonNull myViewHolder holder, int position) {

            hashMap = arrayList.get(position);
            String id = hashMap.get("id");
            String amount = hashMap.get("amount");
            String note = hashMap.get("note");
            String date = hashMap.get("date");

            holder.PayTranAmountTv.setText("Tk "+ String.format("%,.0f", Double.parseDouble(amount)));
            setTextOrHide(holder.PayTranNoteTv, note);
            holder.PayTranDateTv.setText(date);

            holder.LoanPayDeleteBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    AlertDialog.Builder alert = new AlertDialog.Builder(LoanPay.this);

                    View myView = getLayoutInflater().inflate(R.layout.display_delete_alert, null);
                    alert.setView(myView);

                    AlertDialog dialog = alert.create();
                    dialog.setCancelable(false);

                    Button NoBtn = myView.findViewById(R.id.NoBtn);
                    Button YesBtn = myView.findViewById(R.id.YesBtn);

                    YesBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            dbHelper.DeletePaymentList(id);
                            dialog.dismiss();
                            DetailsRefresh();
                            LoadData();
                            Toast.makeText(getApplicationContext(), "Delete Successful", Toast.LENGTH_SHORT).show();

                        }
                    });

                    NoBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            dialog.dismiss();

                        }
                    });

                    dialog.show();

                }
            });

        }

        @Override
        public int getItemCount() {
            return arrayList.size();
        }


    }

    //=============== TextView Hide ===============
    private void setTextOrHide(TextView textView, String text) {
        if (text != null && !text.trim().isEmpty()) {
            textView.setText(text);
            textView.setVisibility(View.VISIBLE);
        } else {
            textView.setVisibility(View.GONE);
        }
    }


}