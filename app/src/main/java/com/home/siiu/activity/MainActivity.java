package com.home.siiu.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.home.siiu.adapter.ContactRecyclerViewAdapter;
import com.home.siiu.adapter.PagerAdapter;
import com.home.siiu.R;
import com.home.siiu.fragment.ContactFragment;
import com.yarolegovich.lovelydialog.LovelyProgressDialog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {


    /*............................................................*/
    /*..........................Variables.........................*/
    /*............................................................*/

    /*..........................Constant..........................*/
    public String photoUrl = "https://firebasestorage.googleapis.com/v0/b/siiu-9f71c.appspot.com/o/default_avata.png?alt=media&token=52371e0d-d820-4225-bedd-4dafd289bfac";
    public int CONTACT_HANDLER = 1;
    public int ADD_HANDLER = 2;

    /*..........................ViewPager.........................*/
    private ViewPager viewPager;

    /*..........................TabLayout.........................*/
    private TabLayout tabLayout;

    /*..........................Firebase..........................*/
    private DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
    private String userId;
    private String userName;
    private int contactsCount;

    /*..........................ArrayList.........................*/
    public List<String> avatarImg  = new ArrayList<>();
    public List<String> userNames = new ArrayList<>();
    public List<String> userIds = new ArrayList<>();

    /*..........................Handler..........................*/
    private Handler handler;

    /*..........................Wait Dialog.......................*/
    public LovelyProgressDialog waitingDialog;

    public int index;
    private ContactFragment fragment = null;


    /*............................................................*/
    /*..........................Functions.........................*/
    /*............................................................*/


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tabLayout = findViewById(R.id.tabLayout);
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        waitingDialog = new LovelyProgressDialog(MainActivity.this).setCancelable(false);
        initViewPager();
        loadContact();
        loadMyInfo();
        showingRequestDialog();
        receiveRequest();
    }

    private void initViewPager() {

        viewPager = findViewById(R.id.Pager);
        PagerAdapter pagerAdapter = new PagerAdapter(getSupportFragmentManager(), tabLayout.getTabCount());
        fragment = pagerAdapter.getContactFragment();
        handler = fragment.handler;
        viewPager.setAdapter(pagerAdapter);

        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    private void loadContact() {

        dbRef.child("contacts").child(userId).child("receive").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                contactsCount = (int)(dataSnapshot.getChildrenCount());
                if (dataSnapshot.exists()) {

                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {

                        String uid = userSnapshot.child("sendUserId").getValue().toString();
                        dbRef.child("users").child(uid).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                if (dataSnapshot.child("photoUrl").exists()) {

                                    avatarImg.add(dataSnapshot.child("photoUrl").getValue().toString());
                                } else {

                                    avatarImg.add(photoUrl);
                                }
                                userNames.add(dataSnapshot.child("userName").getValue().toString());
                                userIds.add(dataSnapshot.getKey());
                                if (avatarImg.size() == contactsCount) {

                                    if (fragment.contactRecyclerView != null) {

                                        handler.sendEmptyMessage(CONTACT_HANDLER);
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                } else {

                    handler.sendEmptyMessage(CONTACT_HANDLER);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void loadMyInfo() {

        dbRef.child("users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                userName = dataSnapshot.child("userName").getValue().toString();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void gotoFragment(int page) {

        viewPager.setCurrentItem(page);
    }

    public void addContact(String imgUrl, String userNm, String userid, int pos) {

        waitingDialog.setIcon(R.drawable.ic_person_low)
                .setTitle("Sending Request...")
                .setTopColorRes(R.color.colorPrimary)
                .show();

        avatarImg.add(imgUrl);
        userNames.add(userNm);
        userIds.add(userId);
        index = pos;

        Map<String, Object> sendResult = new HashMap<>();
        sendResult.put("receiveUserId", userid);
        sendResult.put("receiveUserName", userNm);
        sendResult.put("isAccepted", false);
        dbRef.child("contacts").child(userId).child("sent").push().updateChildren(sendResult);

        Map<String, Object> receiveResult = new HashMap<>();
        receiveResult.put("sendUserId", userId);
        receiveResult.put("sendUserName", userName);
        receiveResult.put("isAccepted", false);
        dbRef.child("contacts").child(userid).child("receive").push().updateChildren(receiveResult);

        handler.sendEmptyMessage(ADD_HANDLER);
    }

    private void showingRequestDialog() {

        dbRef.child("contacts").child(userId).child("receive").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()) {

                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {

                        if (!(Boolean)userSnapshot.child("isAccepted").getValue()) {

                            String sendUserName = userSnapshot.child("sendUserName").getValue().toString();
                            String sendUserId = userSnapshot.child("sendUserId").getValue().toString();
                            showDialog(sendUserName, sendUserId);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void showDialog(String userName, final String userid) {

        AlertDialog.Builder altdial = new AlertDialog.Builder(MainActivity.this);
        altdial.setMessage(userName + " would like to add you as a contact").setCancelable(false)
                .setPositiveButton("Accept", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        acceptReceiverSide(userid);
                    }
                })
                .setNegativeButton("Decline", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {


                    }
                });

        AlertDialog alert = altdial.create();
        alert.setTitle("Contact Request");
        alert.show();
    }


    private void receiveRequest() {

        dbRef.child("contacts").child(userId).child("receive").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                String sendUserName = dataSnapshot.child("sendUserName").getValue().toString();
                String sendUserId = dataSnapshot.child("sendUserId").getValue().toString();
                showDialog(sendUserName, sendUserId);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void acceptReceiverSide(final String userid) {

        dbRef.child("contacts").child(userId).child("receive").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {

                    if (!(Boolean)userSnapshot.child("isAccepted").getValue()) {

                        if (userSnapshot.child("sendUserId").getValue().equals(userid)) {

                            
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void acceptSenderSide() {

    }
}
