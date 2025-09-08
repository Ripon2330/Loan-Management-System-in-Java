package com.bll.loanmanagementsystem;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    private Context context;

    public DatabaseHelper(Context context) {
        super(context, "loan_management_system", null, 1);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL("create table loan_table (id INTEGER primary key autoincrement, name TEXT, amount DOUBLE, note TEXT, date TEXT)");
        db.execSQL("create table loan_pay_table (id INTEGER primary key autoincrement, loan_id INTEGER NOT NULL, amount DOUBLE NOT NULL, note TEXT, date TEXT, FOREIGN KEY (loan_id) REFERENCES loan_table(id) ON DELETE CASCADE);");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL("drop table if exists loan_table");
        db.execSQL("drop table if exists loan_pay_table");

        onCreate(db);

    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }


    //=============== Add Loan List ===============
    public void AddLoanList (String name, Double amount, String note, String date) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues conval = new ContentValues();
        conval.put("name", name);
        conval.put("amount", amount);
        conval.put("note", note);
        conval.put("date", date);

        db.insert("loan_table", null, conval);

    }

    //=============== Show All Due List ===============
    public Cursor ShowAllLoanList() {
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT " +
                "d.id, d.name, d.amount AS total_loan, d.note, d.date, " +
                "IFNULL(SUM(r.amount), 0) AS total_paid, " +
                "(d.amount - IFNULL(SUM(r.amount), 0)) AS remaining_loan " +
                "FROM loan_table d " +
                "LEFT JOIN loan_pay_table r ON d.id = r.loan_id " +
                "GROUP BY d.id, d.name, d.amount, d.note, d.date " +
                "ORDER BY d.id DESC";

        return db.rawQuery(query, null);
    }

    //=============== Update Loan List ===============
    public void UpdateLoanList (String id, String name, Double amount, String note, String date) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues conval = new ContentValues();
        conval.put("name", name);
        conval.put("amount", amount);
        conval.put("note", note);
        conval.put("date", date);

        db.update("loan_table", conval, "id=?", new String[]{id});
        db.close();

    }

    //=============== Delete Loan List ===============
    public void DeleteLoanList(String id) {

        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from loan_table where id = "+id);

    }

    //=============== Show Loan List ===============
    public double[] ShowTotalLoanBalance() {
        double totalLoan = 0;
        double totalPay = 0;
        double remaining = 0;

        SQLiteDatabase db = this.getReadableDatabase();

        // Total Loan
        Cursor cursor1 = db.rawQuery("SELECT SUM(amount) FROM loan_table", null);
        if (cursor1 != null) {
            if (cursor1.moveToFirst()) {
                totalLoan = cursor1.getDouble(0);
            }
            cursor1.close();
        }

        // Total Loan Pay
        Cursor cursor2 = db.rawQuery("SELECT SUM(amount) FROM loan_pay_table", null);
        if (cursor2 != null) {
            if (cursor2.moveToFirst()) {
                totalPay = cursor2.getDouble(0);
            }
            cursor2.close();
        }

        // Remaining
        remaining = totalLoan - totalPay;

        return new double[]{totalLoan, totalPay, remaining};
    }

    //=============== Due Receive Page View ===============
    public Cursor ShowSingleLoanDetails(String id) {
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT " +
                "d.id, d.name, d.amount AS total_loan, d.note, d.date, " +
                "IFNULL(SUM(r.amount), 0) AS total_paid, " +
                "(d.amount - IFNULL(SUM(r.amount), 0)) AS remaining_loan " +
                "FROM loan_table d " +
                "LEFT JOIN loan_pay_table r ON d.id = r.loan_id " +
                "WHERE d.id = ? " +
                "GROUP BY d.id, d.name, d.amount, d.note, d.date";

        return db.rawQuery(query, new String[]{String.valueOf(id)});
    }

    //=============== Loan Pay ===============
    public void LoanPay (String loan_id, Double amount, String note, String date) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues conval = new ContentValues();
        conval.put("loan_id", loan_id);
        conval.put("amount", amount);
        conval.put("note", note);
        conval.put("date", date);

        db.insert("loan_pay_table", null, conval);

    }

    //=============== Show All Receive Data ===============
    public Cursor ShowPaymentData(String loan_id) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT id, loan_id, amount, note, date FROM loan_pay_table WHERE loan_id = ? ORDER BY id DESC";
        return db.rawQuery(query, new String[]{loan_id});
    }

    //=============== Delete Receive List ===============
    public void DeletePaymentList(String id) {

        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from loan_pay_table where id = "+id);

    }

}