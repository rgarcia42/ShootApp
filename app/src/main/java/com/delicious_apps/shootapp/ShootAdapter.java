package com.delicious_apps.shootapp;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.parse.ParseObject;

import org.apache.http.util.ByteArrayBuffer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.util.List;

/**
 * Created by Raymond on 11/24/2014.
 * Shoot app adapter class
 */
public class ShootAdapter extends ArrayAdapter<ParseObject> implements AdapterView.OnItemClickListener {

    View selectedView;


    public ShootAdapter(Context context, int resource, List<ParseObject> items) {
        super(context, resource, items);
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View v = convertView;

        if (v == null) {

            LayoutInflater vi;
            vi = LayoutInflater.from(getContext());
            v = vi.inflate(R.layout.list_row, parent);
        }

        ParseObject shoot = getItem(position);

        if (shoot != null) {

            TextView tt = (TextView) v.findViewById(R.id.shootTitle);
            TextView tt1 = (TextView) v.findViewById(R.id.shootSender);
            TextView tt3 = (TextView) v.findViewById(R.id.shootDate);

            if (tt != null) {
                tt.setText(shoot.getString("name"));
            }
            if (tt1 != null) {

                tt1.setText(shoot.getString("sender"));
            }
            if (tt3 != null) {
                    tt3.setText(shoot.getCreatedAt().toString());
            }
        }
        return v;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Bundle job = new Bundle();
        job.putString("url", getItem(position).getParseFile("file").getUrl());
        job.putString("filename", getItem(position).getString("name"));
        selectedView = view;

        LinearLayout progressView = (LinearLayout) view.findViewById(R.id.progress);
        progressView.setVisibility(View.VISIBLE);

        RetrieveFile fileRetriever = new RetrieveFile();
        fileRetriever.execute(job);

    }

    protected void updateProgress(int percent)
    {
        ProgressBar bar = (ProgressBar)selectedView.findViewById(R.id.downloadprogress);
        bar.setProgress(percent);
    }

    protected void completeProgress()
    {
        selectedView.findViewById(R.id.progress).setVisibility(View.INVISIBLE);
    }

    protected class RetrieveFile extends AsyncTask<Bundle, Integer, Boolean>
    {
        @Override
        protected Boolean doInBackground(Bundle... job) {
            for(int i = 0; i < job.length; i++)
            {
                File sdCard = Environment.getExternalStorageDirectory();
                File dir = new File(sdCard.getAbsolutePath() + "/shot/");
                String filename = job[i].getString("filename");
                if (!dir.exists())
                    dir.mkdir();

                try {
                    java.net.URL fileLocation = new java.net.URL(job[i].getString("url"));
                    URLConnection ucon = fileLocation.openConnection();
                    InputStream stream = ucon.getInputStream();

                    ByteArrayBuffer fileContents = new ByteArrayBuffer(5000);

                    int bytesRead = 0;
                    int current;
                    int totalBytes = ucon.getContentLength();
                    while ((current = stream.read()) != -1) {
                        fileContents.append((byte) current);
                        bytesRead++;
                        float progress = (bytesRead/(float)totalBytes) * 100;
                        updateProgress((int)progress);
                    }

                    FileOutputStream destination = new FileOutputStream(new File(dir, filename));
                    destination.write(fileContents.toByteArray());
                    destination.flush();
                    destination.close();
                } catch (IOException exception) {
                    Log.e("shoot", exception.getMessage());
                }
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            completeProgress();
            super.onPostExecute(aBoolean);
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            updateProgress(values[0]);
            super.onProgressUpdate(values[0]);
        }
    }
}
