//written by Aneesh PA in November 2016
package com.rdcis.pa.addcontactsrdcis_version01;

import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.Context;
import android.content.OperationApplicationException;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.RawContacts;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import android.os.AsyncTask;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {
    private ProgressBar spinner;
    //private Button b1;
    Context cntx;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        cntx = getApplicationContext();

        Button b1 = (Button) findViewById(R.id.button);
        Button b2 = (Button) findViewById(R.id.button2); //for deleting 898688...stuff earlier added
       // Button b3 = (Button) findViewById(R.id.button3); //for deleting 0898688...stuff earlier added

        spinner = (ProgressBar) findViewById(R.id.progressBar1);
        spinner.setVisibility(View.GONE);

        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                spinner.setVisibility(View.VISIBLE);
                new AddContacts().execute();
            }
        });

        b2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                spinner.setVisibility(View.VISIBLE);
                new DelContacts1().execute();           //for deleting 898688...stuff earlier added
            }
        });
/*
        b3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                spinner.setVisibility(View.VISIBLE);
                new AddContacts1().execute();             //for testing; for  adding 898688...stuff

            }
        });
*/

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

        return super.onOptionsItemSelected(item);
    }

//the function that writes contacts
public void WritePhoneContact(String displayName, String design, String epabx, String number, Context cntx ) {
        Context context = cntx; //Application's context or Activity's context
        String strDisplayName = displayName; // Name of the Person to add
        String strNumber = number; //number of the person to add with the Contact
        String strDesign = design; //designation of the person to add with contact
        String strEpabx = epabx; //epabx work phone of the person to add with conbtact

        ArrayList<ContentProviderOperation> cntProOper = new ArrayList<ContentProviderOperation>();
        int contactIndex = cntProOper.size();//ContactSize

        //Newly Inserted contact
        // A raw contact will be inserted ContactsContract.RawContacts table in contacts database.
        cntProOper.add(ContentProviderOperation.newInsert(RawContacts.CONTENT_URI)//Step1
                .withValue(RawContacts.ACCOUNT_TYPE, null)
                .withValue(RawContacts.ACCOUNT_NAME, null).build());

        //Display name will be inserted in ContactsContract.Data table
        cntProOper.add(ContentProviderOperation.newInsert(Data.CONTENT_URI)//Step2
                .withValueBackReference(Data.RAW_CONTACT_ID, contactIndex)
                .withValue(Data.MIMETYPE, StructuredName.CONTENT_ITEM_TYPE)
                .withValue(StructuredName.DISPLAY_NAME, strDisplayName) // Name of the contact
                .build());
        //Mobile number will be inserted in ContactsContract.Data table
        cntProOper.add(ContentProviderOperation.newInsert(Data.CONTENT_URI)//Step 3
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, contactIndex)
                .withValue(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE)
                .withValue(Phone.NUMBER, strNumber) // Number to be added
                .withValue(Phone.TYPE, Phone.TYPE_MOBILE).build()); //Type like HOME, MOBILE etc
        //Designation to be inserted
        cntProOper.add(ContentProviderOperation.newInsert(Data.CONTENT_URI)//Step 4
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, contactIndex)
                .withValue(Data.MIMETYPE, ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.Organization.COMPANY, "RDCIS SAIL")
                .withValue(ContactsContract.CommonDataKinds.Organization.TITLE, strDesign)
                .build());

        //EPABX Work number will be inserted in ContactsContract.Data table
        cntProOper.add(ContentProviderOperation.newInsert(Data.CONTENT_URI)//Step 5
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, contactIndex)
                .withValue(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE)
                .withValue(Phone.NUMBER, strEpabx) // Number to be added
                .withValue(Phone.TYPE, Phone.TYPE_WORK).build()); //work epabx phone

        try {
            // We will do batch operation to insert all above data
            //Contains the output of the app of a ContentProviderOperation.
            //It is sure to have exactly one of uri or count set
            ContentProviderResult[] contentProresult = context.getContentResolver().applyBatch(ContactsContract.AUTHORITY, cntProOper);
        } catch (RemoteException exp) {
            //logs;
        } catch (OperationApplicationException exp) {
            //logs
        }
    }

//the function that deletes contacts
public static boolean deleteContact(Context ctx, String phone, String name) {
        Uri contactUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phone));
        Cursor cur = ctx.getContentResolver().query(contactUri, null, null, null, null);
        try {
            if (cur.moveToFirst()) {
                do {
                    if (cur.getString(cur.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME)).equals(name)) {
                        String lookupKey = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY));
                        Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_LOOKUP_URI, lookupKey);
                        ctx.getContentResolver().delete(uri, null, null);
                        return true;
                    }

                } while (cur.moveToNext());
            }

        } catch (Exception e) {
            System.out.println(e.getStackTrace());
        } finally {
            cur.close();
        }
        return false;
    }

//final version of async addcontacts, with +91 in mob
private class AddContacts extends AsyncTask<String, Integer, Long> {
    @Override
    protected Long doInBackground(String... params) {
        WritePhoneContact("Name", "Designation", "PAX", "Mobile",cntx);
        /* Similar one liner for each contact was generated from a csv
	file using a python script : generator.py*/
        return Long.valueOf(0);

    }

    protected void onProgressUpdate(Integer... progress) {

    }

    protected void onPostExecute(Long result) {
    spinner.setVisibility(View.GONE);
        Toast.makeText(MainActivity.this, "All Contacts Added", Toast.LENGTH_LONG).show();

    }
}
//async del1 contacts, removes that starts with 898688, ie with neither 0 nor +91 as suffix
private class DelContacts1 extends AsyncTask<String, Integer, Long> {
        @Override
        protected Long doInBackground(String... params) {
            return Long.valueOf(0);

        }

        protected void onProgressUpdate(Integer... progress) {

        }

        protected void onPostExecute(Long result) {
            spinner.setVisibility(View.GONE);
            Toast.makeText(MainActivity.this, "R&D Contacts Deleted", Toast.LENGTH_LONG).show();

        }
    }

//async write for testing, adds contacts with 898688
private class AddContacts1 extends AsyncTask<String, Integer, Long> {
    @Override
    protected Long doInBackground(String... params) {
        WritePhoneContact("Name", "Designation", "PAX", "mob",cntx);
                return Long.valueOf(0);

    }

    protected void onProgressUpdate(Integer... progress) {

    }

    protected void onPostExecute(Long result) {
        spinner.setVisibility(View.GONE);
        Toast.makeText(MainActivity.this, "All Contacts Added", Toast.LENGTH_LONG).show();

    }
}
}

