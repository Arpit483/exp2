package com.example.exp2.recycler;

import android.app.DownloadManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.example.exp2.R;

import java.io.File;
import java.util.List;

public class FileAdapter extends RecyclerView.Adapter<FileAdapter.FileViewHolder> {

    private List<String> files;
    private Context context;

    public FileAdapter(List<String> files, Context context) {
        this.files = files;
        this.context = context;
    }

    public void updateFiles(List<String> newFiles) {
        this.files = newFiles;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_file, parent, false);
        return new FileViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FileViewHolder holder, int position) {
        String fileName = files.get(position);
        holder.tvFileName.setText(fileName);

        holder.downloadIcon.setOnClickListener(v -> downloadFile(fileName));
    }

    private void downloadFile(String fileName) {
        String baseUrl = "https://9ed6-2409-40c2-104c-bae3-19d2-b49f-ec06-317a.ngrok-free.app /download/";
        String url = baseUrl + fileName;

        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setTitle("Downloading " + fileName);
        request.setDescription("Excel file download in progress...");
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);

        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);
        request.setMimeType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        long downloadId = downloadManager.enqueue(request);

// ✅ Toast for download started
        Toast.makeText(context, "Download started: " + fileName, Toast.LENGTH_SHORT).show();


        // Create receiver object
        android.content.BroadcastReceiver onComplete = new android.content.BroadcastReceiver() {
            @Override
            public void onReceive(Context ctx, Intent intent) {
                long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                if (id == downloadId) {
                    File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName);
                    Uri fileUri = FileProvider.getUriForFile(context, context.getPackageName() + ".provider", file);

                    Intent openIntent = new Intent(Intent.ACTION_VIEW);
                    openIntent.setDataAndType(fileUri, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
                    openIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_GRANT_READ_URI_PERMISSION);

                    android.app.PendingIntent pendingIntent = android.app.PendingIntent.getActivity(
                            context, 0, openIntent, android.app.PendingIntent.FLAG_IMMUTABLE);

                    android.app.NotificationManager manager = (android.app.NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        android.app.NotificationChannel channel = new android.app.NotificationChannel(
                                "download_channel", "Downloads", android.app.NotificationManager.IMPORTANCE_HIGH);
                        manager.createNotificationChannel(channel);
                    }

                    androidx.core.app.NotificationCompat.Builder builder = new androidx.core.app.NotificationCompat.Builder(context, "download_channel")
                            .setSmallIcon(android.R.drawable.stat_sys_download_done)
                            .setContentTitle("Download complete")
                            .setContentText(fileName)
                            .setAutoCancel(true)
                            .addAction(android.R.drawable.ic_menu_view, "Open", pendingIntent);

                    manager.notify((int) downloadId, builder.build());

                    // ✅ Toast
                    Toast.makeText(context, "Download completed: " + fileName, Toast.LENGTH_SHORT).show();

                    context.unregisterReceiver(this);
                }
            }

        };

        // Register properly with reference
        ContextCompat.registerReceiver(context, onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE), ContextCompat.RECEIVER_NOT_EXPORTED);
    }


    @Override
    public int getItemCount() {
        return files.size();
    }

    static class FileViewHolder extends RecyclerView.ViewHolder {
        TextView tvFileName;
        ImageView downloadIcon;

        public FileViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFileName = itemView.findViewById(R.id.tvFileName);
            downloadIcon = itemView.findViewById(R.id.ivDownload);
        }
    }
}
