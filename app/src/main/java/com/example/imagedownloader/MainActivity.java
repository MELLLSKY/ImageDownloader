package com.example.imagedownloader;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

public class MainActivity extends AppCompatActivity {

    EditText txtURL;
    Button btnDownload;
    ImageView imgView;

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        txtURL = (EditText) findViewById(R.id.txtURL);
        btnDownload = (Button) findViewById(R.id.btnDownload);
        imgView = (ImageView) findViewById(R.id.imgView);
        btnDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int permission = ActivityCompat.checkSelfPermission(
                        MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
                if (permission != PackageManager.PERMISSION_GRANTED) {
                    // We don't have permission so prompt the user
                    ActivityCompat.requestPermissions(
                            MainActivity.this,
                            PERMISSIONS_STORAGE,
                            REQUEST_EXTERNAL_STORAGE
                    );
                }
                /*String fileName = "temp.jpg";
                String imagePath = (Environment.getExternalStoragePublicDirectory
                        (Environment.DIRECTORY_DOWNLOADS)).toString()
                        + "/" + fileName;
                downloadFile(txtURL.getText().toString(),imagePath); preview(imagePath);*/
                else{
                    DownloadTasks backgroundTask = new DownloadTasks();
                    String[] urls = new String[1];
                    urls[0] = txtURL.getText().toString();
                    backgroundTask.execute(urls);
                }

            }
        });
    }
    void downloadFile(String strURL, String imagePath){
        try {
            URL url = new URL(strURL);
            URLConnection connection = url.openConnection();
            connection.connect();

            InputStream inputStream = new BufferedInputStream(url.openStream(), 8192);
            OutputStream outputStream = new FileOutputStream(imagePath);
            byte data[] = new byte[1024];
            int count;
            while ((count = inputStream.read(data)) != -1) {
                outputStream.write(data, 0, count);
            }
            outputStream.flush();
            outputStream.close();
            inputStream.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    void preview(String imagePath ){
        Bitmap bitmapImage = BitmapFactory.decodeFile(imagePath);
        float w =bitmapImage.getWidth();
        float h = bitmapImage.getHeight();
        int W = 400;
        int H = (int) ( (h*W)/w);
        Bitmap b = Bitmap.createScaledBitmap(bitmapImage, W, H, false);
        imgView.setImageBitmap(b);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_EXTERNAL_STORAGE) {
            if (grantResults.length == 2 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                /*String fileName = "temp.jpg";
                String imagePath = (Environment.getExternalStoragePublicDirectory
                        (Environment.DIRECTORY_DOWNLOADS)).toString()
                        + "/" + fileName;
                downloadFile(txtURL.getText().toString(),imagePath);
                preview(imagePath);*/

                DownloadTasks task =new DownloadTasks();
                String[] urls = new String[1];
                urls[0] =txtURL.getText().toString();
                task.execute(urls);



            } else {
                Toast.makeText(this, "External Storage permission not granted",
                        Toast.LENGTH_SHORT).show();
            }
        }

    }
    class DownloadTasks extends AsyncTask<String, Integer, Bitmap>{
        @Override
        protected Bitmap doInBackground(String... strings){
            String fileName = "temp.jpg";
            String imagePath = (Environment.getExternalStoragePublicDirectory
                    (Environment.DIRECTORY_DOWNLOADS)).toString();
            downloadFile(strings[0],imagePath + "/" + fileName);
            return scaleBitmap(imagePath +"/" + fileName);
        }
        @Override
        protected void onPostExecute(Bitmap b) {
            imgView.setImageBitmap(b);
        }
    }
    private Bitmap scaleBitmap(String imagePath){
        Bitmap bitmapImage = BitmapFactory.decodeFile(imagePath);
        float w = bitmapImage.getWidth();
        float h = bitmapImage.getHeight();
        int W = 400;
        int H = (int) ( (h*W)/w);
        Bitmap b = Bitmap.createScaledBitmap(bitmapImage, W, H, false);
        return b;

    }
    class DownloadRunnable implements Runnable{
        String url;

        public DownloadRunnable(String url){
            this.url = url;
        }

        @Override
        public void run(){
            String filename = "temp.jpg";
            String imagePath = (Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)).toString();
            downloadFile(url, imagePath + "/" + filename);
            Bitmap bitmap = scaleBitmap(imagePath + "/"  + filename);
            runOnUiThread(new UpdateBitmap(bitmap));
        }
        class UpdateBitmap implements Runnable{
            Bitmap bitmap;

            public UpdateBitmap(Bitmap bitmap){

            }
            @Override
            public  void run(){
                imgView.setImageBitmap(bitmap);
            }
        }

    }
}