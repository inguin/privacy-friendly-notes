package org.secuso.privacyfriendlynotes;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.database.Cursor;
import android.support.v4.app.NotificationCompat;

/**
 * Created by Robin on 26.06.2016.
 */
public class NotificationService extends IntentService {
    public static final String NOTIFICATION_ID = "notification_id";

    public NotificationService (){
        super("Notification service");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        int notification_id = (int) (long) intent.getLongExtra(NOTIFICATION_ID, -1);
        if (notification_id != -1) {
            //get the cursor on the notification
            Cursor cNotification = DbAccess.getNotification(getBaseContext(), notification_id);
            cNotification.moveToFirst();
            //get all the necessary attributes
            int note_id = cNotification.getInt(cNotification.getColumnIndexOrThrow(DbContract.NotificationEntry.COLUMN_NOTE));

            //get the corresponding note
            Cursor cNote = DbAccess.getNote(getBaseContext(), note_id);
            cNote.moveToFirst();

            //Gather the info for the notification itself
            int type = cNote.getInt(cNote.getColumnIndexOrThrow(DbContract.NoteEntry.COLUMN_TYPE));
            String name = cNote.getString(cNote.getColumnIndexOrThrow(DbContract.NoteEntry.COLUMN_NAME));
            Intent i = null;
            switch (type) {
                case DbContract.NoteEntry.TYPE_TEXT:
                    i = new Intent(getBaseContext(), TextNoteActivity.class);
                    i.putExtra(TextNoteActivity.EXTRA_ID, note_id);
                    i.putExtra(TextNoteActivity.EXTRA_NOTIFICATION_ID, notification_id);
                    break;
                case DbContract.NoteEntry.TYPE_AUDIO:
                    //TODO start the audio note
                    break;
                case DbContract.NoteEntry.TYPE_SKETCH:
                    //TODO start the sketch note
                    break;
                case DbContract.NoteEntry.TYPE_CHECKLIST:
                    Intent i4 = new Intent(getApplication(), ChecklistNoteActivity.class);
                    i4.putExtra(ChecklistNoteActivity.EXTRA_ID, note_id);
                    i4.putExtra(TextNoteActivity.EXTRA_NOTIFICATION_ID, notification_id);
                    break;
            }

            PendingIntent pendingIntent = PendingIntent.getActivity(getBaseContext(), 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getBaseContext());
            mBuilder.setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(name)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true);
            // Sets an ID for the notification
            NotificationManager mNotifyMgr = (NotificationManager) getSystemService(getBaseContext().NOTIFICATION_SERVICE);
            mNotifyMgr.notify(notification_id, mBuilder.build());
            cNote.close();
            cNotification.close();
            //Delete the database entry
            DbAccess.deleteNotification(getBaseContext(), notification_id);
        }
    }
}
