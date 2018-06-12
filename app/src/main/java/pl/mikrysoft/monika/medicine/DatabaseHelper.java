package pl.mikrysoft.monika.medicine;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class DatabaseHelper extends SQLiteOpenHelper{

    public static final String DATABASE_NAME="android.db";
    public static final String TABLE_NAME ="pomiary_table";
    public static final String TABLE_NAME2 ="leki_table";

    public static final String COL_1 ="ID";
    public static final String COL_2 ="DATA";
    public static final String COL_3 ="WAGA";
    public static final String COL_4 ="CISNIENIE";

    public static final String COL_12 ="ID";
    public static final String COL_22 ="LEK";
    public static final String COL_33 ="DAWKA";
    public static final String COL_32 ="RANO";
    public static final String COL_42 ="POLUDNIE";
    public static final String COL_52 ="WIECZOR";


    public DatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);

    }


    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("create table " + TABLE_NAME + " (ID INTEGER PRIMARY KEY AUTOINCREMENT, DATA TEXT, WAGA TEXT, CISNIENIE TEXT)");
        sqLiteDatabase.execSQL("create table " + TABLE_NAME2 + " (ID INTEGER PRIMARY KEY AUTOINCREMENT, LEK TEXT, RANO TEXT, POLUDNIE TEXT, WIECZOR TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(sqLiteDatabase);
    }

    public boolean insertDateLeki (String lek,  String dawka, String rano, String poludnie, String wieczor )
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_22, lek);
        contentValues.put(COL_33, dawka);
        contentValues.put(COL_32, rano);
        contentValues.put(COL_42, poludnie);
        contentValues.put(COL_52, wieczor);
        long result = db.insert(TABLE_NAME2, null, contentValues);
        if(result==-1)
            return false;
        else
            return true;
    }

    public boolean insertDatePomiary (String data, String waga, String cisnienie )
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_2, data);
        contentValues.put(COL_3, waga);
        contentValues.put(COL_4, cisnienie);
        long result = db.insert(TABLE_NAME, null, contentValues);
        if(result==-1)
            return false;
        else
            return true;
    }

    public Cursor getAllMedicineData(){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("SELECT * FROM "+TABLE_NAME2, null );
        return res;
    }

    public Cursor getAllPressureData(){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("SELECT * FROM "+TABLE_NAME, null );
        return res;
    }

}
