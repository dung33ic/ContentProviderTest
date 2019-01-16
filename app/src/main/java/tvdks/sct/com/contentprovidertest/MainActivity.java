package tvdks.sct.com.contentprovidertest;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main,menu);
        return true;
    }

    public void addBirthday(View v){
        // Add a new record
        ContentValues values = new ContentValues();
        values.put(BirthProvider.NAME,((EditText)findViewById(R.id.name)).getText().toString());
        values.put(BirthProvider.BIRTHDAY,((EditText)findViewById(R.id.birthday)).getText().toString());
        Uri uri = getContentResolver().insert(BirthProvider.CONTENT_URI,values);
        Toast.makeText(getBaseContext(),"by dung33ic "+uri.toString()+" inserted! ",Toast.LENGTH_LONG).show();
    }
    public void showAllBirthday(View v){
        String URL = "content://tvdks.sct.com.provider.Birthday/friends";
        Uri friends = Uri.parse(URL);
        Cursor c = getContentResolver().query(friends,null,null,null,"name");
        String result = "Result: ";
        if(!c.moveToFirst()){
            Toast.makeText(this,result + "no content yet!",Toast.LENGTH_LONG).show();
        }else{
            do {
                result = result + "\n" +
                        c.getString(c.getColumnIndex(BirthProvider.NAME)) +
                        " with id " + c.getString(c.getColumnIndex(BirthProvider.ID))+
                        " has birthday: "+c.getString(c.getColumnIndex(BirthProvider.BIRTHDAY));
            }while (c.moveToNext());
            Toast.makeText(this,result,Toast.LENGTH_LONG).show();
        }

    }
    public void deleteAllBirthday(View v){
        //delete all records and the table of the database provider
        String URL = "content://tvdks.sct.com.provider.Birthday/friends";
        Uri friends = Uri.parse(URL);
        int count = getContentResolver().delete(friends, null, null);
        String countNum = "Dung33ic notice: "+ count + "records are delete";
        Toast.makeText(getBaseContext(),countNum,Toast.LENGTH_LONG).show();
    }
}
