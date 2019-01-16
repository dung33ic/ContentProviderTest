package tvdks.sct.com.contentprovidertest;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

import java.util.HashMap;

public class BirthProvider extends ContentProvider {

    // fields for my content provider
    static final String PROVIDER_NAME = "tvdks.sct.com.provider.Birthday";
    static final String URL = "content://" + PROVIDER_NAME + "/friends";
    static final Uri CONTENT_URI = Uri.parse(URL);

    // fields for the database
    static final String ID = "id";
    static final  String NAME = "name";
    static final String BIRTHDAY = "birthday";

    // database declarations
    private SQLiteDatabase database;
    static final String DATABASE_NAME = "BirthdayReminder";
    static final String TABLE_NAME = "birthTable";
    static final int DATABASE_VERSION = 1;
    static final String CREATE_TABLE =
            "CREATE TABLE "+ TABLE_NAME +
                    "(id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "name TEXT NOT NULL, "+
                    "birthday TEXT NOT NULL);";

    // integer values used in content URI
    static final int FRIENDS = 1;
    static final int FRIENDS_ID = 2;

    DBHelper dbHelper;

    // projection map for a query
    private static HashMap<String,String> BirthMap;

    // maps content URI "patterns" to the integer values that were set above
    static final UriMatcher uriMatcher;
        static {
            uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
            uriMatcher.addURI(PROVIDER_NAME,"friends",FRIENDS);
            uriMatcher.addURI(PROVIDER_NAME,"friends/#",FRIENDS_ID);
        }


    public BirthProvider() {
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Implement this to handle requests to delete one or more rows.
        int count = 0;
        switch (uriMatcher.match(uri)){
            case FRIENDS:
                //delete all the records of the table
                count = database.delete(TABLE_NAME,selection,selectionArgs);
                break;
            case  FRIENDS_ID:
                String id = uri.getLastPathSegment(); //gets the id
                database.delete(TABLE_NAME,ID + "="+ id + (TextUtils.isEmpty(selection)? "AND ("+ selection + ')':""),selectionArgs);
                break;
             default: throw new IllegalArgumentException("Unsupport URI " + uri);
        }
        getContext().getContentResolver().notifyChange(uri,null);
        return count;
    }

    @Override
    public String getType(Uri uri) {
        // TODO: Implement this to handle requests for the MIME type of the data
        // at the given URI.
        switch (uriMatcher.match(uri)){
            // Get aLL friend-birthday records
            case FRIENDS:
                return "vnd.android.cursor.dir/vnd.tvdks.sct.com.provider.Birthday.friends";
             //get a particular friend
            case FRIENDS_ID:
                return "vnd.android.cursor.item/vnd.tvdks.sct.com.provider.Birthday.friends";
             default:throw new IllegalArgumentException("Unsupport URI"+uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        // TODO: Implement this to handle requests to insert a new row.
        long row = database.insert(TABLE_NAME,"",values);

        // If record is added successfully
        if (row>0){
            Uri newUri = ContentUris.withAppendedId(CONTENT_URI,row);
            getContext().getContentResolver().notifyChange(newUri, null);
            return newUri;
        }
        throw new SQLException("Fail to add a new record into "+ uri);
    }

    @Override
    public boolean onCreate() {
        // TODO: Implement this to initialize your content provider on startup.
        Context context = getContext();
        dbHelper = new DBHelper(context);
        database = dbHelper.getWritableDatabase();
        return (database == null) ? false : true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        // TODO: Implement this to handle query requests from clients.
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(TABLE_NAME);

        switch (uriMatcher.match(uri)){
            // maps all database column names
            case FRIENDS:
                qb.setProjectionMap(BirthMap);
                break;
            case FRIENDS_ID:
                qb.appendWhere(ID + "="+ uri.getLastPathSegment());
                break;
            default:throw new IllegalArgumentException("Unknow URI"+ uri);
        }
        // No sorting-> sort on names by default
        if (sortOrder==null||sortOrder=="") {
            sortOrder = NAME;
        }
        Cursor cursor = qb.query(database,projection,selection,selectionArgs,null,null,sortOrder);
        /*register to watch a content URI for changes*/
        cursor.setNotificationUri(getContext().getContentResolver(),uri);
        return cursor;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        // TODO: Implement this to handle requests to update one or more rows.
        int count = 0;
        switch (uriMatcher.match(uri)){
            case FRIENDS:
                count = database.update(TABLE_NAME,values,selection,selectionArgs);
                break;
            case FRIENDS_ID:
                count = database.update(TABLE_NAME,values,ID+"="+uri.getLastPathSegment()+(!TextUtils.isEmpty(selection)? "AND ("+selection+')':""),selectionArgs);
                break;
             default:throw new IllegalArgumentException("Unsupport URI"+uri)   ;
        }
        getContext().getContentResolver().notifyChange(uri,null);
        return count;
    }

    private static class DBHelper extends SQLiteOpenHelper {

        public DBHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(CREATE_TABLE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS "+TABLE_NAME);
            onCreate(db);
        }
    }
}
