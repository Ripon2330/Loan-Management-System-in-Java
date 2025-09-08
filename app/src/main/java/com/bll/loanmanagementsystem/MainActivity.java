package com.bll.loanmanagementsystem;

import static android.app.ProgressDialog.show;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
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
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    DatabaseHelper dbHelper;
    MaterialToolbar BackIcon;
    TextView NoDataFoundTv;
    RecyclerView LoanListRecyclerView;
    FloatingActionButton LoanFabBtn;
    TextView LoanTotalTv, LoanPayTv, LoanRemainingTv;
    HashMap<String,String> hashMap;
    ArrayList<HashMap<String, String>> arrayList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        dbHelper = new DatabaseHelper(MainActivity.this);
        BackIcon = findViewById(R.id.BackIcon);
        NoDataFoundTv = findViewById(R.id.NoDataFoundTv);
        LoanListRecyclerView = findViewById(R.id.LoanListRecyclerView);
        LoanFabBtn = findViewById(R.id.LoanFabBtn);
        LoanTotalTv = findViewById(R.id.LoanTotalTv);
        LoanPayTv = findViewById(R.id.LoanPayTv);
        LoanRemainingTv = findViewById(R.id.LoanRemainingTv);
        LoadData();
        TotalDueBalance();

        //=============== Back Page Button ===============//
        BackIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                finish();

            }
        });

        LoanFabBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);

                View myView = getLayoutInflater().inflate(R.layout.add_loan_list, null);
                alert.setView(myView);

                final AlertDialog dialog = alert.create();
                dialog.setCancelable(false);

                TextInputEditText edLoanName= myView.findViewById(R.id.edLoanName);
                TextInputEditText edLoanAmount= myView.findViewById(R.id.edLoanAmount);
                TextInputEditText edLoanNote= myView.findViewById(R.id.edLoanNote);
                TextInputEditText edLoanDate= myView.findViewById(R.id.edLoanDate);
                Button AddBtn = myView.findViewById(R.id.AddBtn);
                Button CloseBtn= myView.findViewById(R.id.CloseBtn);

                //=============== Date Picker ===============
                SimpleDateFormat sdf = new SimpleDateFormat("dd-MMMM-yyyy", Locale.getDefault());
                String todayDate = sdf.format(Calendar.getInstance().getTime());
                edLoanDate.setText(todayDate);

                edLoanDate.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Calendar calendar = Calendar.getInstance();

                        DatePickerDialog datePickerDialog = new DatePickerDialog(MainActivity.this,
                                new DatePickerDialog.OnDateSetListener() {
                                    @Override
                                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                                        Calendar selectedCalendar = Calendar.getInstance();
                                        selectedCalendar.set(year, month, dayOfMonth);

                                        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMMM-yyyy", Locale.getDefault());
                                        String selectedDate = sdf.format(selectedCalendar.getTime());
                                        edLoanDate.setText(selectedDate);
                                    }
                                },
                                calendar.get(Calendar.YEAR),
                                calendar.get(Calendar.MONTH),
                                calendar.get(Calendar.DAY_OF_MONTH)
                        );

                        datePickerDialog.show();
                    }
                });


                AddBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        String name = edLoanName.getText().toString().trim();
                        String sAmount = edLoanAmount.getText().toString().trim();
                        String note = edLoanNote.getText().toString().trim();
                        String date = edLoanDate.getText().toString().trim();

                        if (TextUtils.isEmpty(edLoanName.getText().toString())){
                            edLoanName.setError("Required");
                            return;
                        }

                        if (TextUtils.isEmpty(edLoanAmount.getText().toString())){
                            edLoanAmount.setError("Required");
                            return;
                        }


                        Double amount = Double.parseDouble(sAmount);
                        dbHelper.AddLoanList(name, amount, note, date);
                        dialog.dismiss();
                        LoadData();
                        TotalDueBalance();
                        Toast.makeText(getApplicationContext(), "Data Insert Successful!"), Toast.LENGTH_SHORT).show();

                    }
                });

                CloseBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        dialog.dismiss();

                    }
                });

                dialog.show();

            }
        });


    }

    //=============== Load Loan Balance ===============//
    private void TotalDueBalance() {
        double[] loanStats = dbHelper.ShowTotalLoanBalance();

        double totalLoan = loanStats[0];
        double totalPay = loanStats[1];
        double remainingLoan = loanStats[2];

        LoanTotalTv.setText("Tk "+ String.format("%,.0f", totalLoan));
        LoanPayTv.setText("Tk "+ String.format("%,.0f", totalPay));
        LoanRemainingTv.setText("Tk "+ String.format("%,.0f", remainingLoan));
    }

    //=============== Load Data ===============//
    private void LoadData() {

        Cursor cursor = dbHelper.ShowAllLoanList();
        if (cursor!=null && cursor.getCount()>0 ){

            arrayList = new ArrayList<>();

            while (cursor.moveToNext()){

                int id = cursor.getInt(0);
                String name = cursor.getString(1);
                double total_loan = cursor.getDouble(2);
                String note = cursor.getString(3);
                String date = cursor.getString(4);
                double total_paid = cursor.getDouble(5);
                double remaining_loan = cursor.getDouble(6);

                hashMap = new HashMap<>();
                hashMap.put("id", String.valueOf(id));
                hashMap.put("name", name);
                hashMap.put("total_loan", String.valueOf(total_loan));
                hashMap.put("total_paid", String.valueOf(total_paid));
                hashMap.put("remaining_loan", String.valueOf(remaining_loan));
                hashMap.put("note", note);
                hashMap.put("date", date);

                arrayList.add(hashMap);

            }

            NoDataFoundTv.setVisibility(View.GONE);
            LoanAdapter adapter = new LoanAdapter();
            LoanListRecyclerView.setAdapter(adapter);
            LoanListRecyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));

        }else {

            NoDataFoundTv.setVisibility(View.VISIBLE);

        }

    }

    //=============== Adapter ===============//
    private class LoanAdapter extends RecyclerView.Adapter <LoanAdapter.myViewHolder> {
        private class myViewHolder extends RecyclerView.ViewHolder{
            TextView DisBankNameTv, DisLoanBalanceTv, LoanPayTv, LoanRemainingTv, LoanNoteTv, LoanDateTv;
            LinearLayout LoanToPayPage, LoanInfoUpdateBtn;

            public myViewHolder(@NonNull View itemView) {
                super(itemView);

                DisBankNameTv = itemView.findViewById(R.id.DisBankNameTv);
                DisLoanBalanceTv = itemView.findViewById(R.id.DisLoanBalanceTv);
                LoanPayTv = itemView.findViewById(R.id.LoanPayTv);
                LoanRemainingTv = itemView.findViewById(R.id.LoanRemainingTv);
                LoanNoteTv = itemView.findViewById(R.id.LoanNoteTv);
                LoanDateTv = itemView.findViewById(R.id.LoanDateTv);
                LoanInfoUpdateBtn = itemView.findViewById(R.id.LoanInfoUpdateBtn);
                LoanToPayPage = itemView.findViewById(R.id.LoanToPayPage);

            }
        }

        @NonNull
        @Override
        public myViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

            LayoutInflater inflater = getLayoutInflater();
            View myView = inflater.inflate(R.layout.display_loan_list, parent, false);

            return new myViewHolder(myView);
        }

        @Override
        public void onBindViewHolder(@NonNull myViewHolder holder, int position) {

            final HashMap<String, String> currentMap = arrayList.get(position);
            final String loan_id = currentMap.get("id");
            String name = currentMap.get("name");
            String total_loan = currentMap.get("total_loan");
            String total_paid = currentMap.get("total_paid");
            String remaining_loan = currentMap.get("remaining_loan");
            String note = currentMap.get("note");
            String date = currentMap.get("date");

            holder.DisBankNameTv.setText(name);
            holder.DisLoanBalanceTv.setText(": Tk" + String.format("%,.0f", Double.parseDouble(total_loan)));
            holder.LoanPayTv.setText(": Tk" + String.format("%,.0f", Double.parseDouble(total_paid)));
            holder.LoanRemainingTv.setText(": Tk" + String.format("%,.0f", Double.parseDouble(remaining_loan)));
            holder.LoanDateTv.setText(date);
            setTextOrHide(holder.LoanNoteTv, note);

            //=============== Due List Update/Delete ===============
            final int pos = position;
            holder.LoanInfoUpdateBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);

                    View myView = getLayoutInflater().inflate(R.layout.update_loan_list, null);
                    alert.setView(myView);

                    AlertDialog mainDialog = alert.create();
                    mainDialog.setCancelable(true);

                    TextInputEditText edUpdateLoanName = myView.findViewById(R.id.edUpdateLoanName);
                    TextInputEditText edUpdateLoanAmount = myView.findViewById(R.id.edUpdateLoanAmount);
                    TextInputEditText edUpdateLoanNote = myView.findViewById(R.id.edUpdateLoanNote);
                    TextInputEditText edUpdateLoanDate = myView.findViewById(R.id.edUpdateLoanDate);
                    Button LoanDeleteBtn = myView.findViewById(R.id.LoanDeleteBtn);
                    Button LoanUpdateBtn = myView.findViewById(R.id.LoanUpdateBtn);

                    //===== Due List Update View
                    hashMap = arrayList.get(pos);
                    String name = hashMap.get("name");
                    String amount = hashMap.get("total_loan");
                    String note = hashMap.get("note");
                    String date = hashMap.get("date");

                    edUpdateLoanName.setText(name);
                    edUpdateLoanAmount.setText(amount);
                    edUpdateLoanNote.setText(note);
                    edUpdateLoanDate.setText(date);

                    //=============== Date Picker ===============
                    edUpdateLoanDate.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Calendar calendar = Calendar.getInstance();

                            DatePickerDialog datePickerDialog = new DatePickerDialog(MainActivity.this,
                                    new DatePickerDialog.OnDateSetListener() {
                                        @Override
                                        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                                            Calendar selectedCalendar = Calendar.getInstance();
                                            selectedCalendar.set(year, month, dayOfMonth);

                                            SimpleDateFormat sdf = new SimpleDateFormat("dd-MMMM-yyyy", Locale.getDefault());
                                            String selectedDate = sdf.format(selectedCalendar.getTime());
                                            edUpdateLoanDate.setText(selectedDate);
                                        }
                                    },
                                    calendar.get(Calendar.YEAR),
                                    calendar.get(Calendar.MONTH),
                                    calendar.get(Calendar.DAY_OF_MONTH)
                            );

                            datePickerDialog.show();
                        }
                    });

                    //=============== Due List Update ===============
                    LoanUpdateBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            String name = edUpdateLoanName.getText().toString().trim();
                            String sAmount = edUpdateLoanAmount.getText().toString().trim();
                            String note = edUpdateLoanNote.getText().toString().trim();
                            String date = edUpdateLoanDate.getText().toString().trim();

                            if (TextUtils.isEmpty(edUpdateLoanName.getText().toString())){
                                edUpdateLoanName.setError("Required");
                                return;
                            }

                            if (TextUtils.isEmpty(edUpdateLoanAmount.getText().toString())){
                                edUpdateLoanAmount.setError("Required");
                                return;
                            }

                            Double amount = Double.parseDouble(sAmount);
                            dbHelper.UpdateLoanList(loan_id, name, amount, note, date);
                            mainDialog.dismiss();
                            LoadData();
                            TotalDueBalance();
                            Toast.makeText(getApplicationContext(), "Info Update Successful!", Toast.LENGTH_SHORT).show();

                        }
                    });

                    //=============== Due List Delete ===============
                    LoanDeleteBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);

                            View myView = getLayoutInflater().inflate(R.layout.display_delete_alert, null);
                            alert.setView(myView);

                            AlertDialog confirmDialog = alert.create();
                            confirmDialog.setCancelable(false);

                            Button NoBtn = myView.findViewById(R.id.NoBtn);
                            Button YesBtn = myView.findViewById(R.id.YesBtn);

                            YesBtn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                    dbHelper.DeleteLoanList(loan_id);
                                    confirmDialog.dismiss();
                                    mainDialog.dismiss();
                                    LoadData();
                                    TotalDueBalance();
                                    Toast.makeText(getApplicationContext(), "Delete Successful!", Toast.LENGTH_SHORT).show();

                                }
                            });

                            NoBtn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                    confirmDialog.dismiss();

                                }
                            });

                            confirmDialog.show();

                        }
                    });

                    mainDialog.show();

                }
            });

            //=============== Loan Receive Page ===============
            holder.LoanToPayPage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent intent = new Intent(MainActivity.this, LoanPay.class);
                    intent.putExtra("loan_id", currentMap.get("id"));
                    startActivity(intent);

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

    //=============== Balance Back Call ===============
    @Override
    public void onResume() {
        super.onResume();

        LoadData();
        TotalDueBalance();

    }
}