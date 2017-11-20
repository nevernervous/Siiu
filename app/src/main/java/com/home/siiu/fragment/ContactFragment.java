package com.home.siiu.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.home.siiu.R;
import com.home.siiu.activity.MainActivity;
import com.home.siiu.adapter.ContactRecyclerViewAdapter;
import com.yarolegovich.lovelydialog.LovelyProgressDialog;

import java.util.ArrayList;
import java.util.List;

public class ContactFragment extends Fragment {


    /*............................................................*/
    /*..........................Variables.........................*/
    /*............................................................*/

    /*..........................Constant..........................*/
    public String photoUrl = "https://firebasestorage.googleapis.com/v0/b/siiu-9f71c.appspot.com/o/default_avata.png?alt=media&token=52371e0d-d820-4225-bedd-4dafd289bfac";
    public int CONTACT_HANDLER = 1;
    public int ADD_HANDLER = 2;

    /*..........................Firebase..........................*/
    private DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
    private String userId;

    /*..........................ArrayList.........................*/
    private List<String> searchedUserPhoto = new ArrayList<>();
    private List<String> searchedUserName = new ArrayList<>();
    private List<String> searchedUserId = new ArrayList<>();

    /*..........................Component.........................*/
    public RecyclerView contactRecyclerView;
    public FloatingActionButton searchBarBtn;
    public LinearLayout searchLayout;
    public EditText searchNameEditText;
    public Button searchBtn;
    public ImageView closeBtn;

    /*..........................Wait Dialog.......................*/
    public LovelyProgressDialog waitingDialog;

    /*.................ContactRecyclerViewAdapter.................*/
    public ContactRecyclerViewAdapter adapter;

    MainActivity activity = null;


    /*............................................................*/
    /*..........................Functions.........................*/
    /*............................................................*/


    public Handler handler = new Handler() {

        public void handleMessage(Message msg) {

            if (msg.what == CONTACT_HANDLER) {

                setRecyclerViewAdapter(activity.avatarImg, activity.userNames, activity.userIds, false);
                waitingDialog.dismiss();
            } else if (msg.what == ADD_HANDLER) {

                searchedUserPhoto.remove(activity.index);
                searchedUserName.remove(activity.index);
                searchedUserId.remove(activity.index);
                setRecyclerViewAdapter(searchedUserPhoto, searchedUserName, searchedUserId, true);
                activity.waitingDialog.dismiss();
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_contact, container, false);

        initData(view);
        setContactRecyclerView(view);
        setSearchUserClick(view);
        setSearchBtnClick(view);
        setCloseBtnClick(view);

        waitingDialog.setIcon(R.drawable.ic_person_low)
                .setTitle("Loading Contact...")
                .setTopColorRes(R.color.colorPrimary)
                .show();

        return view;
    }

    private void initData(View view) {

        activity = (MainActivity) getActivity();
        searchLayout = view.findViewById(R.id.SearchLayout);
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        waitingDialog = new LovelyProgressDialog(getContext()).setCancelable(false);
    }

    private void setContactRecyclerView(View view) {

        contactRecyclerView = view.findViewById(R.id.ContactRecyclerView);
        adapter = new ContactRecyclerViewAdapter(getContext(), activity.avatarImg, activity.userNames, activity.userIds, false);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        contactRecyclerView.setLayoutManager(layoutManager);
    }

    private void setSearchUserClick(View view) {

        searchBarBtn = view.findViewById(R.id.ShowSearchBar);
        searchBarBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                searchLayout.setVisibility(View.VISIBLE);
                searchNameEditText.setText("");

                setRecyclerViewAdapter(new ArrayList<String>(), new ArrayList<String>(), new ArrayList<String>(), true);
            }
        });
    }

    private void setSearchBtnClick(View view) {

        searchBtn = view.findViewById(R.id.SearchBtn);
        searchNameEditText = view.findViewById(R.id.SearchEditText);
        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                searchedUserPhoto.clear();
                searchedUserName.clear();
                dbRef.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {

                            if (activity.userIds.size() > 0) {

                                for (int i = 0; i < activity.userIds.size(); i++) {

                                    if (!userSnapshot.getKey().equals(activity.userIds.get(i))) {

                                        String userName = userSnapshot.child("userName").getValue().toString();
                                        if (userName.toLowerCase().contains(searchNameEditText.getText().toString().toLowerCase())) {

                                            if (!userId.equals(userSnapshot.getKey())) {

                                                if (userSnapshot.child("photoUrl").exists()) {

                                                    searchedUserPhoto.add(userSnapshot.child("photoUrl").getValue().toString());
                                                } else {

                                                    searchedUserPhoto.add(photoUrl);
                                                }
                                                searchedUserName.add(userName);
                                                searchedUserId.add(userSnapshot.getKey());
                                            }
                                        }
                                    }
                                }
                            } else {

                                String userName = userSnapshot.child("userName").getValue().toString();
                                if (userName.toLowerCase().contains(searchNameEditText.getText().toString().toLowerCase())) {

                                    if (!userId.equals(userSnapshot.getKey())) {

                                        if (userSnapshot.child("photoUrl").exists()) {

                                            searchedUserPhoto.add(userSnapshot.child("photoUrl").getValue().toString());
                                        } else {

                                            searchedUserPhoto.add(photoUrl);
                                        }
                                        searchedUserName.add(userName);
                                        searchedUserId.add(userSnapshot.getKey());
                                    }
                                }
                            }
                        }
                        setRecyclerViewAdapter(searchedUserPhoto, searchedUserName, searchedUserId, true);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        });
    }

    private void setCloseBtnClick(View view) {

        closeBtn = view.findViewById(R.id.CloseBtn);
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                searchLayout.setVisibility(View.GONE);

                setRecyclerViewAdapter(activity.avatarImg, activity.userNames, activity.userIds, false);
            }
        });
    }

    private void setRecyclerViewAdapter(List<String> imgs, List<String> names, List<String> ids, Boolean search) {

        adapter.imgName = imgs;
        adapter.userNames = names;
        adapter.userIds = ids;
        adapter.isSearchOrContact = search;
        contactRecyclerView.setAdapter(adapter);
    }
}
