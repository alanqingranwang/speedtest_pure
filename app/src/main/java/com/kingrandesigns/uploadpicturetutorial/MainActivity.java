package com.kingrandesigns.uploadpicturetutorial;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String SERVER_ADDRESS = "http://qwang78.web.engr.illinois.edu/";
    Button bUploadImage, bDownloadImage, bDownload2sec, bUpload2sec;
    ProgressBar progressBar;
    TextView downloadSpeed;
    TextView uploadSpeed;
    TextView latitude;
    TextView longitude;
    ToggleButton togglebutton;
    SharedPreferences sp;

    private static final double BYTE_TO_KILOBIT = 0.0078125;
    private static final double KILOBIT_TO_MEGABIT = 0.0009765625;
    private static final double TIME_TO_MEASURE_FOR = 3 * 1000;
    public static String fileName = "SavedData";
    private double mlatitude;
    private double mlongitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
/*
        String address = SERVER_ADDRESS + "Pride%20and%20Prejudice.txt";

        long start = System.currentTimeMillis();

        try {
            //set the download URL, a url that points to a file on the internet
            //this is the file to be downloaded
            URL url = new URL(address);

            //create the new connection
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

            //set up some things on the connection
            urlConnection.setRequestMethod("GET");
            urlConnection.setDoOutput(true);

            //and connect!
            urlConnection.connect();

            //set the path where we want to save the file
            //in this case, going to save it on the root directory of the
            //sd card.
            File SDCardRoot = Environment.getExternalStorageDirectory();
            //create a new file, specifying the path, and the filename
            //which we want to save the file as.
            File file = new File(SDCardRoot, "pride_and_prejudice.txt");

            //this will be used to write the downloaded data into the file we created
            FileOutputStream fileOutput = new FileOutputStream(file);

            //this will be used in reading the data from the internet
            InputStream inputStream = urlConnection.getInputStream();

            //create a buffer...
            byte[] buffer = new byte[1024];
            int bufferLength = 0; //used to store a temporary size of the buffer

            //now, read through the input buffer and write the contents to the file
            while ((bufferLength = inputStream.read(buffer)) > 0) {
                //add the data in the buffer to the file in the file output stream (the file on the sd card
                fileOutput.write(buffer, 0, bufferLength);
                //add up the size so we know how much is downloaded
                //this is where you would do something to report the prgress, like this maybe
            }

            fileOutput.close();

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
*/
        bUploadImage = (Button) findViewById(R.id.bUploadImage);
        bDownloadImage = (Button) findViewById(R.id.bDownloadImage);
        bDownload2sec = (Button) findViewById(R.id.bDownload2sec);
        bUpload2sec = (Button) findViewById(R.id.bUpload2sec);

        bUploadImage.setOnClickListener(this);
        bDownloadImage.setOnClickListener(this);
        bDownload2sec.setOnClickListener(this);
        bUpload2sec.setOnClickListener(this);

        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        progressBar.setMax(100);

        downloadSpeed = (TextView) findViewById(R.id.downloadSpeed);
        uploadSpeed = (TextView) findViewById(R.id.uploadSpeed);
        latitude = (TextView) findViewById(R.id.latitude);
        longitude = (TextView) findViewById(R.id.longitude);


        togglebutton = (ToggleButton) findViewById(R.id.toggleButton);
        togglebutton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                File SDCardRoot = Environment.getExternalStorageDirectory();
                //create a new file, specifying the path, and the filename
                //which we want to save the file as.
                final File file = new File(SDCardRoot, "pride_and_prejudice.txt");
                final Handler handler = new Handler();
                Timer timer = new Timer();
                TimerTask doAsynchronousTask = null;
                if (isChecked) {
                    doAsynchronousTask = new TimerTask() {
                        @Override
                        public void run() {
                            handler.post(new Runnable() {
                                public void run() {
                                    try {
                                        new Download2(getApplicationContext()).execute();
                                        new Upload2(SERVER_ADDRESS + "SavePicture.php", file, getApplicationContext()).execute();

                                        sp = getSharedPreferences(fileName, 0);
                                        SharedPreferences.Editor editor = sp.edit();
                                        Map<String, ?> dataReturned = sp.getAll();
                                        int length = dataReturned.size();
                                        editor.putString(length + " l", "" + mlatitude + ", " + mlongitude);
                                        editor.commit();

                                    } catch (Exception e) {
                                        // TODO Auto-generated catch block
                                    }
                                }
                            });
                        }
                    };
                    timer.schedule(doAsynchronousTask, 0, 10000);


                } else {
                    doAsynchronousTask.cancel();
                }
            }
        });

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                // Called when a new location is found by the network location provider.
                mlatitude = location.getLatitude();
                mlongitude = location.getLongitude();
                latitude.setText(mlatitude + "");
                longitude.setText(mlongitude + "");
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {}

            public void onProviderEnabled(String provider) {}

            public void onProviderDisabled(String provider) {}
        };

// Register the listener with the Location Manager to receive location updates
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
    }

    @Override
    public void onClick(View v) {
        File SDCardRoot = Environment.getExternalStorageDirectory();
        //create a new file, specifying the path, and the filename
        //which we want to save the file as.
        File file = new File(SDCardRoot, "pride_and_prejudice.txt");
        switch (v.getId()) {
            case R.id.bDownloadImage:
                new Download(getApplicationContext()).execute();
                break;
            case R.id.bUploadImage:
                new Upload(SERVER_ADDRESS + "SavePicture.php", file, getApplicationContext()).execute();
                break;
            case R.id.bDownload2sec:
                new Download2(getApplicationContext()).execute();
                break;
            case R.id.bUpload2sec:
                new Upload2(SERVER_ADDRESS + "SavePicture.php", file, getApplicationContext()).execute();
                break;
        }
    }

    private class Download extends AsyncTask<Void, Integer, SpeedInfo> {

        Context context;

        public Download(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            progressBar.setMax(100);
        }

        @Override
        protected SpeedInfo doInBackground(Void... params) {
            String address = SERVER_ADDRESS + "War%20and%20Peace.txt";

            long start = System.currentTimeMillis();

            try {
                //set the download URL, a url that points to a file on the internet
                //this is the file to be downloaded
                URL url = new URL(address);

                //create the new connection
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                //set up some things on the connection
                urlConnection.setRequestMethod("GET");
                urlConnection.setDoOutput(true);

                //and connect!
                urlConnection.connect();

                //set the path where we want to save the file
                //in this case, going to save it on the root directory of the
                //sd card.
                File SDCardRoot = Environment.getExternalStorageDirectory();
                //create a new file, specifying the path, and the filename
                //which we want to save the file as.
                File file = new File(SDCardRoot, "war_and_peace.txt");

                //this will be used to write the downloaded data into the file we created
                FileOutputStream fileOutput = new FileOutputStream(file);

                //this will be used in reading the data from the internet
                InputStream inputStream = urlConnection.getInputStream();

                //this is the total size of the file
                int totalSize = urlConnection.getContentLength();
                //variable to store total downloaded bytes
                int downloadedSize = 0;

                //create a buffer...
                byte[] buffer = new byte[1024];
                int bufferLength = 0; //used to store a temporary size of the buffer

                //now, read through the input buffer and write the contents to the file
                while ((bufferLength = inputStream.read(buffer)) > 0) {
                    //add the data in the buffer to the file in the file output stream (the file on the sd card
                    fileOutput.write(buffer, 0, bufferLength);
                    //add up the size so we know how much is downloaded
                    downloadedSize += bufferLength;
                    //this is where you would do something to report the prgress, like this maybe
                    publishProgress((int) (downloadedSize * 100 / totalSize));
                }

                fileOutput.close();

                long downloadTime = (System.currentTimeMillis() - start);
                //Prevent AritchmeticException

                SpeedInfo info = calculate(downloadTime, totalSize);
                return info;

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            progressBar.setProgress(values[0]);
        }

        @Override
        protected void onPostExecute(SpeedInfo result) {
            if (result == null) {
                Toast.makeText(context, "Download error: " + result, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(context, "File downloaded", Toast.LENGTH_SHORT).show();
                downloadSpeed.setText(result.megabits + "");
            }
        }
    }

    private class Upload extends AsyncTask<Void, Integer, SpeedInfo> implements DialogInterface.OnCancelListener {

        private String url;
        private File file;
        Context context;

        public Upload(String url, File file, Context context) {
            this.url = url;
            this.file = file;
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            progressBar.setMax((int) file.length());
        }

        @Override
        protected SpeedInfo doInBackground(Void... v) {
            HttpURLConnection.setFollowRedirects(false);
            HttpURLConnection connection = null;
            String fileName = file.getName();
            long start = System.currentTimeMillis();
            try {
                connection = (HttpURLConnection) new URL(url).openConnection();
                connection.setRequestMethod("POST");
                String boundary = "---------------------------boundary";
                String tail = "\r\n--" + boundary + "--\r\n";
                connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
                connection.setDoOutput(true);

                String metadataPart = "--" + boundary + "\r\n"
                        + "Content-Disposition: form-data; name=\"metadata\"\r\n\r\n"
                        + "" + "\r\n";

                String fileHeader1 = "--" + boundary + "\r\n"
                        + "Content-Disposition: form-data; name=\"uploadfile\"; filename=\""
                        + fileName + "\"\r\n"
                        + "Content-Type: application/octet-stream\r\n"
                        + "Content-Transfer-Encoding: binary\r\n";

                long fileLength = file.length() + tail.length();
                String fileHeader2 = "Content-length: " + fileLength + "\r\n";
                String fileHeader = fileHeader1 + fileHeader2 + "\r\n";
                String stringData = metadataPart + fileHeader;

                long requestLength = stringData.length() + fileLength;
                connection.setRequestProperty("Content-length", "" + requestLength);
                connection.setFixedLengthStreamingMode((int) requestLength);
                connection.connect();

                DataOutputStream out = new DataOutputStream(connection.getOutputStream());
                out.writeBytes(stringData);
                out.flush();

                int progress = 0;
                int bytesRead;
                byte buf[] = new byte[1024];
                InputStream bufInput = new FileInputStream(file);
                while ((bytesRead = bufInput.read(buf)) != -1) {
                    // write output
                    out.write(buf, 0, bytesRead);
                    out.flush();
                    progress += bytesRead;
                    // update progress bar
                    publishProgress(progress);
                }

                // Write closing boundary and close stream
                out.writeBytes(tail);
                out.flush();

                long uploadTime = System.currentTimeMillis() - start;

                SpeedInfo info = calculate(uploadTime, 6190192);
                // Get server response

                return info;

            } catch (Exception e) {
                // Exception
            } finally {
                if (connection != null) connection.disconnect();
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            progressBar.setProgress((int) (progress[0]));
        }

        @Override
        protected void onPostExecute(SpeedInfo info) {
            if (info == null) {
                Toast.makeText(context, "Upload error: " + info, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "File uploaded", Toast.LENGTH_SHORT).show();
                uploadSpeed.setText(info.megabits + "");
            }
        }

        @Override
        public void onCancel(DialogInterface dialog) {
            cancel(true);
            dialog.dismiss();
        }
    }

    private class Download2 extends AsyncTask<Void, Integer, SpeedInfo> {

        Context context;

        public Download2(Context context) {
            this.context = context;
        }

        @Override
        protected SpeedInfo doInBackground(Void... params) {
            String address = SERVER_ADDRESS + "War%20and%20Peace.txt";
            long start = System.currentTimeMillis();

            try {
                //set the download URL, a url that points to a file on the internet
                //this is the file to be downloaded
                URL url = new URL(address);

                //create the new connection
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                //set up some things on the connection
                urlConnection.setRequestMethod("GET");
                urlConnection.setDoOutput(true);

                //and connect!
                urlConnection.connect();

                //set the path where we want to save the file
                //in this case, going to save it on the root directory of the
                //sd card.
                File SDCardRoot = Environment.getExternalStorageDirectory();
                //create a new file, specifying the path, and the filename
                //which we want to save the file as.
                File file = new File(SDCardRoot, "war_and_peace.txt");

                //this will be used to write the downloaded data into the file we created
                FileOutputStream fileOutput = new FileOutputStream(file);
                InputStream input = urlConnection.getInputStream();
                //this will be used in reading the data from the internet
                //create a buffer...
                byte data[] = new byte[4096];
                long total = 0;
                int count;

                while ((System.currentTimeMillis() - start) < TIME_TO_MEASURE_FOR) {
                    count = input.read(data);
                    // allow canceling with back button
                    if (isCancelled()) {
                        input.close();
                        return null;
                    }
                    total += count;
                    // publishing the progress....
                    //if (fileLength > 0) // only if total length is known
                    //    publishProgress((int) (total * 100 / fileLength));
                    //output.write(data, 0, count);
                }

                input.close();

                //long downloadTime = (System.currentTimeMillis() - start);
                //Prevent AritchmeticException

                //SpeedInfo info = calculate(downloadTime, fileLength);
                SpeedInfo info = calculate(System.currentTimeMillis() - start, total);
                return info;

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(SpeedInfo result) {
            if (result == null) {
                Toast.makeText(context, "Download error: " + result, Toast.LENGTH_LONG).show();
                sp = getSharedPreferences(fileName, 0);
                SharedPreferences.Editor editor = sp.edit();
                Map<String, ?> dataReturned = sp.getAll();
                int length = dataReturned.size();
                editor.putString(length+" d", "null");
                editor.commit();
                try {
                    Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                    Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
                    r.play();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(context, "File downloaded", Toast.LENGTH_SHORT).show();
                String speed = result.megabits + "";
                downloadSpeed.setText(speed);
                sp = getSharedPreferences(fileName, 0);
                SharedPreferences.Editor editor = sp.edit();
                Map<String, ?> dataReturned = sp.getAll();
                int length = dataReturned.size();
                editor.putString(length+" d", speed);
                editor.commit();
            }
        }
    }

    private class Upload2 extends AsyncTask<Void, Integer, SpeedInfo> implements DialogInterface.OnCancelListener {

        private String url;
        private File file;
        Context context;

        public Upload2(String url, File file, Context context) {
            this.url = url;
            this.file = file;
            this.context = context;
        }

        @Override
        protected SpeedInfo doInBackground(Void... v) {
            HttpURLConnection.setFollowRedirects(false);
            HttpURLConnection connection = null;
            String fileName = file.getName();
            long start = System.currentTimeMillis();
            try {
                connection = (HttpURLConnection) new URL(url).openConnection();
                connection.setRequestMethod("POST");
                String boundary = "---------------------------boundary";
                String tail = "\r\n--" + boundary + "--\r\n";
                connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
                connection.setDoOutput(true);

                String metadataPart = "--" + boundary + "\r\n"
                        + "Content-Disposition: form-data; name=\"metadata\"\r\n\r\n"
                        + "" + "\r\n";

                String fileHeader1 = "--" + boundary + "\r\n"
                        + "Content-Disposition: form-data; name=\"uploadfile\"; filename=\""
                        + fileName + "\"\r\n"
                        + "Content-Type: application/octet-stream\r\n"
                        + "Content-Transfer-Encoding: binary\r\n";

                long fileLength = file.length() + tail.length();
                String fileHeader2 = "Content-length: " + fileLength + "\r\n";
                String fileHeader = fileHeader1 + fileHeader2 + "\r\n";
                String stringData = metadataPart + fileHeader;

                long requestLength = stringData.length() + fileLength;
                connection.setRequestProperty("Content-length", "" + requestLength);
                connection.setFixedLengthStreamingMode((int) requestLength);
                connection.connect();

                DataOutputStream out = new DataOutputStream(connection.getOutputStream());
                out.writeBytes(stringData);
                out.flush();

                double progress = 0;
                int bytesRead = 0;
                byte buf[] = new byte[1024];
                InputStream bufInput = new FileInputStream(file);
                while ((System.currentTimeMillis() - start) < TIME_TO_MEASURE_FOR) {
                    // write output
                    bytesRead = bufInput.read(buf);
                    out.write(buf, 0, bytesRead);
                    out.flush();
                    if ((System.currentTimeMillis() - start) > 1000) {
                        progress += bytesRead;
                    }
                }

                // Write closing boundary and close stream
                out.writeBytes(tail);
                out.flush();


                SpeedInfo info = calculate(TIME_TO_MEASURE_FOR, progress);
                // Get server response

                return info;

            } catch (Exception e) {
                // Exception
                Log.i("something went wrong", e.toString());
            } finally {
                if (connection != null) connection.disconnect();
            }

            return null;
        }

        @Override
        protected void onPostExecute(SpeedInfo info) {
            if (info == null) {
                Toast.makeText(context, "Upload error: " + info, Toast.LENGTH_SHORT).show();
                sp = getSharedPreferences(fileName, 0);
                SharedPreferences.Editor editor = sp.edit();
                Map<String, ?> dataReturned = sp.getAll();
                int length = dataReturned.size();
                editor.putString(length+" d", "null");
                editor.commit();
                try {
                    Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                    Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
                    r.play();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(context, "File uploaded", Toast.LENGTH_SHORT).show();
                String speed = info.megabits + "";
                uploadSpeed.setText(speed);
                sp = getSharedPreferences(fileName, 0);
                SharedPreferences.Editor editor = sp.edit();
                Map<String, ?> dataReturned = sp.getAll();
                int length = dataReturned.size();
                editor.putString(length + " u", speed);
                editor.commit();
            }
        }

        @Override
        public void onCancel(DialogInterface dialog) {
            cancel(true);
            dialog.dismiss();
        }
    }

    private SpeedInfo calculate(final double downloadTime, final double bytesIn) {
        SpeedInfo info = new SpeedInfo();
        //from mil to sec
        double bytespersecond = (bytesIn / downloadTime) * 1000;
        double kilobits = bytespersecond * BYTE_TO_KILOBIT;
        double megabits = kilobits * KILOBIT_TO_MEGABIT;
        info.downspeed = bytespersecond;
        info.kilobits = kilobits;
        info.megabits = megabits;

        return info;
    }

    private static class SpeedInfo {
        public double kilobits = 0;
        public double megabits = 0;
        public double downspeed = 0;
    }
}



