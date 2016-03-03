package com.coda.contenproviderdemo;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;


public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }


    public void addBirthday(View view) {
        ContentValues values = new ContentValues();

        values.put(BirthProvider.NAME,(( EditText)findViewById(R.id.nameET)).getText().toString());
        values.put(BirthProvider.BIRTHDAY,((EditText)findViewById(R.id.dateET)).getText().toString());

        Uri uri = getContentResolver().insert(BirthProvider.CONTENT_URI, values);

        Toast.makeText(getBaseContext(), "Coda: " + uri.toString() + " inserted.", Toast.LENGTH_LONG).show();
    }

    public void showAllBirthday(View view) {
        String URL = BirthProvider.PROVIDER_NAME;
        Uri friends = Uri.parse(URL);
        Cursor c = getContentResolver().query(friends,null,null,null,"name");
        String results = "Coda results: ";

        if(!c.moveToFirst()){
            Toast.makeText(this, results = "No Content Yet", Toast.LENGTH_LONG).show();

        }else {

            do{
                results =results+"\n"+c.getString(c.getColumnIndex(BirthProvider.NAME))
                + " with id "+ c.getString(c.getColumnIndex(BirthProvider.ID))
                + " has birthday: " + c.getString(c.getColumnIndex(BirthProvider.BIRTHDAY));

            }while (c.moveToNext());

            Toast.makeText(this, results, Toast.LENGTH_LONG).show();
        }

    }

    public void deleteAllBirthdays(View view) {
        String URL = BirthProvider.PROVIDER_NAME;
        Uri friends = Uri.parse(URL);
        int count= getContentResolver().delete(friends, null, null);
        String countNum = "Coda: "+count+ " records are deleted";

        Toast.makeText(getBaseContext(),countNum, Toast.LENGTH_LONG).show();

    }
}
