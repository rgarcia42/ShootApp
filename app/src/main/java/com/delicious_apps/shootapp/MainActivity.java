package com.delicious_apps.shootapp;

import android.content.ContentResolver;
import android.content.Intent;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.MimeTypeMap;
import android.widget.ListView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ProgressCallback;
import com.parse.SaveCallback;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends ActionBarActivity {

    final protected String APP_ID = "3RA7sBo3Kp2r2DN79cvQooeAp1bpzIjYLuCxR92d";
    final protected String CLIENT_KEY = "q2DevviCWRIIjzfkLQSubNimFGCPBVr352TYuPWg";
    final protected int READ_REQUEST_CODE = 42;
    protected ListView recentShoots;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        recentShoots = (ListView)findViewById(R.id.recentShoots);

        //initialize parse for app
        Parse.initialize(this, APP_ID, CLIENT_KEY);

        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("text/plain".equals(type)) {
                handleSendText(intent); // Handle text being sent
                Log.e("filetype", "txt");
            }else if(type.contains("image/")) {
                handleSend(intent); // Handle text being sent
                Log.e("filetype", "img");
            }else if(type.contains("audio/")){
                handleSend(intent); // Handle text being sent
                Log.e("filetype", "audio");

            }else if(type.contains("video/")){
                handleSend(intent); // Handle text being sent
                Log.e("filetype", "video");

            }else if(type.contains("application/vnd.android.package-archive")){
                handleSend(intent); // Handle text being sent
                Log.e("filetype", "apk");

            }else if(type.contains("application/zip")){
                handleSend(intent); // Handle text being sent
                Log.e("filetype", "zip");

            }else if(type.contains("application/msword")){
                handleSend(intent); // Handle text being sent
                Log.e("filetype", "word");

            }else if(type.contains("application/mspowerpoint")){
                handleSend(intent); // Handle text being sent
                Log.e("filetype", "ppt");

            }else if(type.contains("application/msexcel")){
                handleSend(intent); // Handle text being sent
                Log.e("filetype", "excel");

            }else if(type.contains("application/pdf")){
                handleSend(intent); // Handle text being sent
                Log.e("filetype", "pdf");

            }else {
                handleSend(intent); // Handle general send
                Log.e("filetype", type);
            }

        } else if (Intent.ACTION_SEND_MULTIPLE.equals(action) && type != null) {
            handleSendMultiple(intent); // Handle multiple images being sent
        }
        else {
            // Handle other intents, such as being started from the home screen
            ParseQuery<ParseObject> query = ParseQuery.getQuery("Shoot");
            query.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> parseObjects, ParseException e) {
                    if(e == null){
                        populateListView(parseObjects);
                    }
                }
            });
        }

    }

    public void populateListView(List<ParseObject> objects){
        ListView view = (ListView)findViewById(R.id.recentShoots);
        ShootAdapter adapter = new ShootAdapter(this, R.layout.list_row, objects);
        view.setAdapter(adapter);
        view.setOnItemClickListener(adapter);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData){
        if(resultData != null)
            parse(requestCode, resultCode, resultData.getData());
    }

    public void parse(int requestCode, int resultCode, Uri fileLocation){
        if(requestCode == READ_REQUEST_CODE && resultCode == RESULT_OK)
        {
            try {
                InputStream iStream = getContentResolver().openInputStream(fileLocation);

                ParseFile sendFile = new ParseFile("testFile", getRealPath(iStream));

                sendFile.saveInBackground(new SaveCallback() {
                    public void done(ParseException e) {

                    }
                }, new ProgressCallback(){
                    public void done(Integer integer) {
                        //testView.setText(integer.toString());
                        if(integer == 100)
                        {
                            Toast.makeText(getApplicationContext(), "File shot successfully", Toast.LENGTH_LONG).show();
                        }
                    }
                });

                ParseObject shootObject = new ParseObject("Shoot");
                shootObject.put("name", getFileName(fileLocation));
                shootObject.put("file", sendFile);
                shootObject.put("type", getMimeType(fileLocation));
                shootObject.saveInBackground();
            }
            catch(IOException e){
                //also takes care of FIleNotFound exception...

            }
        }
    }

    public String getMimeType(Uri file){
        ContentResolver cr = this.getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();

        String ext = mime.getExtensionFromMimeType(cr.getType(file));

        if(ext != null)
            return ext;
        else {
            return "none";
        }
    }

    public String getFileName(Uri uri){
        String result = null;

        //We are checking the uri for a filename, and if the filename is not specified,
        //we will then simply cut off the final part of the uri and use that as the filename.

        //check for filename
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            if (cursor != null && cursor.moveToFirst())
                result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
        }
        //cut off end since filename was not specified
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        //return filename
        return result;
    }


    public byte[] getRealPath(InputStream inputStream) throws IOException
    {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        try {
            byte[] buffer = new byte[bufferSize];

            int len;
            while ((len = inputStream.read(buffer)) != -1) {
                byteBuffer.write(buffer, 0, len);
            }
        }
        catch(OutOfMemoryError e)
        {//exception thrown if the file is too big to shoot
            Toast.makeText(this, "File too large to shoot", Toast.LENGTH_LONG).show();
            return null;
        }
        return byteBuffer.toByteArray();
    }

    void handleSendText(Intent intent) {
        String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
        if (sharedText != null) {
            // Update UI to reflect text being shared
            ParseObject shootText = new ParseObject("Shoot");
            shootText.put("name", sharedText);
            shootText.put("type", "txt");
            shootText.saveInBackground();
        }
    }

    void handleSend(Intent intent) {
        Uri imageUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
        if(imageUri != null)
            parse(READ_REQUEST_CODE, RESULT_OK, imageUri);
    }


    void handleSendMultiple(Intent intent) {
        ArrayList<Uri> imageUris = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
        if (imageUris != null) {
            // Update UI to reflect multiple images being shared
            for(int i=imageUris.size()-1; !imageUris.isEmpty(); i--)
                parse(READ_REQUEST_CODE, RESULT_OK, imageUris.remove(i));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        if(id == R.id.shoot_new){
            Intent fileBrowser = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            fileBrowser.addCategory(Intent.CATEGORY_OPENABLE);
            fileBrowser.setType("*/*");
            startActivityForResult(fileBrowser, READ_REQUEST_CODE);
        }
        return super.onOptionsItemSelected(item);
    }
}
